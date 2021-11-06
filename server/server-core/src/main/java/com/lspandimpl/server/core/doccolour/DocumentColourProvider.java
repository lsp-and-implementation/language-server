package com.lspandimpl.server.core.doccolour;

import com.lspandimpl.server.api.context.BalDocumentColourContext;
import com.lspandimpl.server.ballerina.compiler.workspace.CompilerManager;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import org.eclipse.lsp4j.Color;
import org.eclipse.lsp4j.ColorInformation;
import org.eclipse.lsp4j.ColorPresentation;
import org.eclipse.lsp4j.ColorPresentationParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DocumentColourProvider {
    private DocumentColourProvider() {
    }

    public static List<ColorInformation> getColours(BalDocumentColourContext context) {
        List<ColorInformation> colorInformations = new ArrayList<>();
        Path path = context.getPath();
        CompilerManager compilerManager = context.compilerManager();
        Document document = compilerManager.getDocument(path).orElseThrow();
        SemanticModel semanticModel = compilerManager.getSemanticModel(path).orElseThrow();
        SyntaxTree syntaxTree = compilerManager.getSyntaxTree(path).orElseThrow();
        ModulePartNode modulePartNode = syntaxTree.rootNode();

        NodeList<ModuleMemberDeclarationNode> members = (modulePartNode).members();

        List<FunctionDefinitionNode> functions = members.stream()
                .filter(member -> member.kind() == SyntaxKind.FUNCTION_DEFINITION)
                .map(member -> (FunctionDefinitionNode) member)
                .collect(Collectors.toList());

        for (FunctionDefinitionNode function : functions) {
            SeparatedNodeList<ParameterNode> parameters = function.functionSignature().parameters();
            Optional<Symbol> symbol = semanticModel.symbol(function.functionName());
            // Iterate over parameters
            for (int i = 0; i < parameters.size(); i++) {
                ParameterNode param = parameters.get(i);
                if (param.kind() != SyntaxKind.REQUIRED_PARAM) {
                    continue;
                }
                RequiredParameterNode reqParm = (RequiredParameterNode) param;
                Optional<AnnotationNode> colourAnnotation
                        = reqParm.annotations().stream()
                        .filter(node ->
                                ((SimpleNameReferenceNode) node.annotReference())
                                        .name().text().equals("Colour"))
                        .findAny();
                if (colourAnnotation.isEmpty()) {
                    continue;
                }
                // get all the references of function
                // and capture the colour values
                List<Location> references =
                        semanticModel.references(symbol.get(), false);

                // Convert RGB string to 
                Map<Range, List<Double>> rgbInfo = getRGB(references, i, context, path);
                rgbInfo.forEach((range, floats) -> {
                    Color color = new Color(floats.get(0), floats.get(1), floats.get(2), floats.get(3));
                    ColorInformation colorInformation = new ColorInformation(range, color);
                    colorInformations.add(colorInformation);
                });

            }
        }
        return colorInformations;
    }

    public static List<ColorPresentation> getColourPresentation(ColorPresentationParams params) {
        TextEdit textEdit = new TextEdit();
        Color color = params.getColor();
        textEdit.setRange(params.getRange());
        String insertColour = "\"" + Math.round(color.getRed() * 255) + ", " +
                Math.round(color.getGreen()) * 255 + ", " +
                Math.round(color.getBlue()) * 255 + ", " +
                Math.round(color.getAlpha()) + "\"";
        textEdit.setNewText(insertColour);
        ColorPresentation presentation = new ColorPresentation();
        presentation.setLabel(insertColour);
        presentation.setTextEdit(textEdit);

        return Collections.singletonList(presentation);
    }

    private static Map<Range, List<Double>> getRGB(List<Location> references, int paramId,
                                                   BalDocumentColourContext context, Path path) {
        Map<Range, List<Double>> colourInfo = new HashMap<>();
        for (Location location : references) {
            LinePosition refStart = location.lineRange().startLine();
            Node node = context.compilerManager().getNode(path, refStart.line(), refStart.offset()).orElseThrow();
            if (node.parent().kind() != SyntaxKind.FUNCTION_CALL) {
                continue;
            }
            SeparatedNodeList<FunctionArgumentNode> args = ((FunctionCallExpressionNode) node.parent()).arguments();
            if (args.isEmpty()) {
                continue;
            }
            FunctionArgumentNode fArg = args.get(paramId);
            ExpressionNode expression = ((PositionalArgumentNode) fArg).expression();
            if (expression.kind() == SyntaxKind.STRING_LITERAL) {
                String rgbValues = ((BasicLiteralNode) expression).literalToken().text().replace("\"", "");
                String[] components = rgbValues.split(",");
                List<Double> parts = new ArrayList<>();
                for (int i = 0; i < components.length; i++) {
                    if (i < components.length - 1) {
                        int integer = Integer.parseInt(components[i].trim());
                        double xx = integer / 255D;
                        parts.add(xx);
                    } else {
                        parts.add(Double.parseDouble(components[i].trim()));
                    }
                }

                LinePosition startLine = expression.lineRange().startLine();
                LinePosition endLine = expression.lineRange().endLine();
                Position start = new Position(startLine.line(), startLine.offset());
                Position end = new Position(endLine.line(), endLine.offset());
                colourInfo.put(new Range(start, end), parts);
            }
        }

        return colourInfo;
    }
}
