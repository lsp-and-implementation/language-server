package com.lspandimpl.server.core.docsymbol;

import com.lspandimpl.server.api.context.BalDocumentSymbolContext;
import com.lspandimpl.server.core.utils.CommonUtils;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.text.LinePosition;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.SymbolTag;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DocumentSymbolProvider {
    public static List<Either<SymbolInformation, DocumentSymbol>>
    getSymbolInformation(BalDocumentSymbolContext context) {
        SyntaxTree syntaxTree = context.compilerManager()
                .getSyntaxTree(context.getPath()).orElseThrow();
        List<Either<SymbolInformation, DocumentSymbol>> symbols =
                new ArrayList<>();
        NodeList<ModuleMemberDeclarationNode> members =
                ((ModulePartNode) syntaxTree.rootNode()).members();
        String uri = context.getPath().toUri().toString();
        for (ModuleMemberDeclarationNode member : members) {
            SymbolInformation funcInfo = new SymbolInformation();
            if (member.kind() == SyntaxKind.FUNCTION_DEFINITION) {
                FunctionDefinitionNode functionDef =
                        (FunctionDefinitionNode) member;
                LinePosition startLine = member.lineRange().startLine();
                LinePosition endLine = member.lineRange().endLine();

                funcInfo.setKind(SymbolKind.Function);
                funcInfo.setName((functionDef).functionName().text());
                Range range = new Range();
                range.setStart(new Position(startLine.line(),
                        startLine.offset()));
                range.setEnd(new Position(endLine.line(),
                        endLine.offset()));
                Location funcLocation = new Location(uri, range);
                funcInfo.setLocation(funcLocation);
                if (isDeprecatedFunction(context, functionDef)) {
                    funcInfo.setTags(Collections.singletonList(SymbolTag.Deprecated));
                }

                // Generate the symbols for the function parameters
                SeparatedNodeList<ParameterNode> parameters =
                        functionDef.functionSignature().parameters();
                for (ParameterNode parameter : parameters) {
                    String paramName;
                    SymbolInformation paramInfo = new SymbolInformation();
                    if (parameter.kind() == SyntaxKind.REQUIRED_PARAM) {
                        paramName = ((RequiredParameterNode) parameter)
                                .paramName().get().text();
                    } else if (parameter.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
                        paramName = ((DefaultableParameterNode) parameter)
                                .paramName().get().text();
                    } else if (parameter.kind() == SyntaxKind.REST_PARAM) {
                        paramName = ((RestParameterNode) parameter)
                                .paramName().get().text();
                    } else {
                        continue;
                    }

                    LinePosition paramStart =
                            parameter.lineRange().startLine();
                    LinePosition paramEnd =
                            parameter.lineRange().endLine();

                    paramInfo.setKind(SymbolKind.TypeParameter);
                    paramInfo.setName(paramName);
                    Range paramRange = new Range();
                    paramRange.setStart(new Position(paramStart.line(),
                            paramStart.offset()));
                    paramRange.setEnd(new Position(paramEnd.line(),
                            paramEnd.offset()));
                    paramInfo.setLocation(new Location(uri, paramRange));
                /*
                Add the parameter under the function symbol
                to represent the hierarchy
                 */
                    paramInfo.setContainerName(funcInfo.getName());

                    symbols.add(Either.forLeft(paramInfo));
                }

                symbols.add(Either.forLeft(funcInfo));
            }
        }

        return symbols;
    }

    public static List<Either<SymbolInformation, DocumentSymbol>>
    getDocumentSymbol(BalDocumentSymbolContext context) {
        SyntaxTree syntaxTree = context.compilerManager()
                .getSyntaxTree(context.getPath()).orElseThrow();
        List<Either<SymbolInformation, DocumentSymbol>> symbols =
                new ArrayList<>();
        NodeList<ModuleMemberDeclarationNode> members =
                ((ModulePartNode) syntaxTree.rootNode()).members();
        for (ModuleMemberDeclarationNode member : members) {
            if (!isRecordTypeDefinition(member)) {
                continue;
            }
            DocumentSymbol recordSymbol = new DocumentSymbol();
            TypeDefinitionNode typeDef =
                    (TypeDefinitionNode) member;

            Token typeName = typeDef.typeName();

            LinePosition startLine = member.lineRange().startLine();
            LinePosition endLine = member.lineRange().endLine();

            LinePosition selectSLine =
                    typeName.lineRange().startLine();
            LinePosition selectELine =
                    typeName.lineRange().endLine();

            recordSymbol.setKind(SymbolKind.Struct);
            recordSymbol.setName(typeName.text());

            Range range = new Range();
            Range selectionRange = new Range();

            range.setStart(new Position(startLine.line(),
                    startLine.offset()));
            range.setEnd(new Position(endLine.line(),
                    endLine.offset()));

            selectionRange.setStart(new Position(selectSLine.line(),
                    selectSLine.offset()));
            selectionRange.setEnd(new Position(selectELine.line(),
                    selectELine.offset()));

            recordSymbol.setRange(range);
            recordSymbol.setSelectionRange(selectionRange);

            // Generate the symbols for the record fields
            NodeList<Node> fields = ((RecordTypeDescriptorNode)
                    typeDef.typeDescriptor()).fields();
            List<DocumentSymbol> children = new ArrayList<>();

            for (Node field : fields) {
                Token fieldName;
                DocumentSymbol fieldSymbol = new DocumentSymbol();
                if (field.kind() == SyntaxKind.RECORD_FIELD_WITH_DEFAULT_VALUE) {
                    fieldName = ((RecordFieldWithDefaultValueNode) field)
                            .fieldName();
                } else if (field.kind() == SyntaxKind.RECORD_FIELD) {
                    fieldName = ((RecordFieldNode) field).fieldName();
                } else {
                    continue;
                }

                LinePosition fieldStart =
                        field.lineRange().startLine();
                LinePosition fieldEnd =
                        field.lineRange().endLine();

                LinePosition fieldSelectStart =
                        fieldName.lineRange().startLine();
                LinePosition fieldSelectEnd =
                        fieldName.lineRange().endLine();

                fieldSymbol.setKind(SymbolKind.Field);
                fieldSymbol.setName(fieldName.text());

                Range fieldRange = new Range();
                Range fieldSelectRange = new Range();

                fieldRange.setStart(new Position(fieldStart.line(),
                        fieldStart.offset()));
                fieldRange.setEnd(new Position(fieldEnd.line(),
                        fieldEnd.offset()));
                fieldSelectRange.setStart(new Position(fieldSelectStart.line(),
                        fieldSelectStart.offset()));
                fieldSelectRange.setEnd(new Position(fieldSelectEnd.line(),
                        fieldSelectEnd.offset()));

                fieldSymbol.setRange(fieldRange);
                fieldSymbol.setSelectionRange(fieldSelectRange);
                    
                /*
                Add the field as a child
                 */
                children.add(fieldSymbol);
            }
            recordSymbol.setChildren(children);
            if (CommonUtils.isDeprecated(member)) {
                recordSymbol.setTags(Collections
                        .singletonList(SymbolTag.Deprecated));
            }
            symbols.add(Either.forRight(recordSymbol));
        }

        return symbols;
    }
    
    private static boolean isDeprecatedFunction(BalDocumentSymbolContext context, FunctionDefinitionNode functionNode) {
        SemanticModel semanticModel = context.compilerManager().getSemanticModel(context.getPath()).orElseThrow();
        Optional<Symbol> symbol = semanticModel.symbol(functionNode);
        if (symbol.isEmpty()) {
            return false;
        }
        return ((FunctionSymbol) symbol.get()).annotations().stream()
                .anyMatch(annotationSymbol -> annotationSymbol.getName()
                        .orElse("").equals("deprecated"));
    }

    private static boolean isRecordTypeDefinition(ModuleMemberDeclarationNode memberDeclarationNode) {
        return memberDeclarationNode.kind() == SyntaxKind.TYPE_DEFINITION
                && ((TypeDefinitionNode) memberDeclarationNode).typeDescriptor().kind() == SyntaxKind.RECORD_TYPE_DESC;
    }
}
