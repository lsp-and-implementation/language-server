package org.lsp.server.core.codelens;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.lsp.server.api.context.BalCodeActionContext;
import org.lsp.server.api.context.BalCodeLensContext;
import org.lsp.server.core.codeaction.BalCommand;
import org.lsp.server.core.codeaction.CommandArgument;

import java.util.ArrayList;
import java.util.Collections;
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
            codeLens.setCommand(command);
            // The range is set to the function name.
            // It is a must, that the range spans for a single line
            codeLens.setRange(toRange(function.functionName().lineRange()));

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

    private static org.eclipse.lsp4j.Command getCreateVarCommand(Range range) {
        org.eclipse.lsp4j.Command command = new org.eclipse.lsp4j.Command();
        command.setCommand(BalCommand.CREATE_VAR.getCommand());
        command.setTitle(BalCommand.CREATE_VAR.getTitle());
        List<Object> args = new ArrayList<>();
        String typeDescriptor = getExpectedTypeDescriptor(range);
        args.add(new CommandArgument("type", typeDescriptor));
        command.setArguments(args);

        return command;
    }

    private static CodeAction
    getCreateVarCodeAction(BalCodeActionContext context,
                           Range range, Diagnostic diagnostic,
                           CodeActionParams params) {
        CodeAction codeAction = new CodeAction();
        codeAction.setTitle(BalCommand.CREATE_VAR.getTitle());
        codeAction.setKind(CodeActionKind.QuickFix);
        /*
        Setting the diagnostic will show a quickfix link when hover over
        the diagnostic
         */
        codeAction.setDiagnostics(Collections
                .singletonList(toDiagnostic(diagnostic)));
        codeAction.setEdit(getWorkspaceEdit(context, range, params));

        return codeAction;
    }

    private static WorkspaceEdit getWorkspaceEdit(BalCodeActionContext context, Range range, CodeActionParams params) {
        return null;
    }

    private static org.eclipse.lsp4j.Diagnostic toDiagnostic(Diagnostic diagnostic) {
        return null;
    }

    private static String getExpectedTypeDescriptor(Range range) {
        return "";
    }

    private static Diagnostic getDiagnostic(Range range) {
        return null;
    }

    private static Position toPosition(LinePosition linePosition) {
        Position position = new Position();
        position.setLine(linePosition.line());
        position.setCharacter(linePosition.offset());

        return position;
    }

    private static Range toRange(LineRange lineRange) {
        Range range = new Range();
        range.setStart(toPosition(lineRange.startLine()));
        range.setEnd(toPosition(lineRange.endLine()));

        return range;
    }
}
