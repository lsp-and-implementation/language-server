package org.lsp.server.core.docsymbol;

import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.lsp.server.api.context.BalDocumentSymbolContext;

import java.util.ArrayList;
import java.util.List;

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
            Location funcLocation =new Location(uri, range);
            funcInfo.setLocation(funcLocation);

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
}
