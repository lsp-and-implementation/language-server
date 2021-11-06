package com.lspandimpl.server.core.codelens;

import com.lspandimpl.server.api.context.BalCodeLensContext;
import com.lspandimpl.server.core.codeaction.BalCommand;
import com.lspandimpl.server.core.codeaction.CommandArgument;
import com.lspandimpl.server.core.utils.CommonUtils;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import com.lspandimpl.server.core.executecommand.AddDocsArgs;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CodeLensProvider {

    public static List<CodeLens>
    getCodeLenses(BalCodeLensContext context, CodeLensParams params) {
        List<FunctionDefinitionNode> functions = getPublicFunctions(context);
        List<CodeLens> codeLensList = new ArrayList<>();
        for (FunctionDefinitionNode function : functions) {
            CodeLens codeLens = new CodeLens();
            org.eclipse.lsp4j.Command command = new org.eclipse.lsp4j.Command();
            command.setCommand(BalCommand.ADD_DOC.getCommand());
            command.setTitle(BalCommand.ADD_DOC.getTitle());

            List<Object> args = new ArrayList<>();
            String fName = function.functionName().text();
            String uri = context.getPath().toUri().toString();
            args.add(new CommandArgument("params", new AddDocsArgs(fName, uri)));
            command.setArguments(args);
            codeLens.setCommand(command);
            // The range is set to the function name.
            // It is a must, that the range spans for a single line
            codeLens.setRange(CommonUtils.toRange(function.functionName().lineRange()));

            codeLensList.add(codeLens);
        }

        return codeLensList;
    }

    private static List<FunctionDefinitionNode> getPublicFunctions(BalCodeLensContext context) {
        SyntaxTree syntaxTree = context.currentSyntaxTree().orElseThrow();
        return ((ModulePartNode) syntaxTree.rootNode()).members().stream()
                .filter(member -> member.kind() == SyntaxKind.FUNCTION_DEFINITION)
                .map(member -> (FunctionDefinitionNode) member)
                .collect(Collectors.toList());
    }
}
