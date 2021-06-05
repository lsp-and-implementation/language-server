package org.lsp.server.core.codeaction;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.lsp.server.api.context.BalCodeActionContext;
import org.lsp.server.api.context.LSContext;
import org.lsp.server.core.configdidchange.ConfigurationHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CodeActionProvider {
    private static final String VAR_ASSIGNMENT_REQUIRED = "";

    public static List<Either<org.eclipse.lsp4j.Command, CodeAction>>
    getCodeAction(BalCodeActionContext context, CodeActionParams params) {
        List<Either<org.eclipse.lsp4j.Command, CodeAction>> codeActions =
                new ArrayList<>();
        Diagnostic diag = getDiagnostic(params.getRange());
        Node topLevelNode = getTopLevelNode(params.getRange());
        String message = diag.message().toLowerCase(Locale.ROOT);
        if (message.equals(VAR_ASSIGNMENT_REQUIRED)) {
            org.eclipse.lsp4j.Command createVarCommand = getCreateVarCommand(params.getRange());
            Either<org.eclipse.lsp4j.Command, CodeAction> command = Either.forLeft(createVarCommand);

            return Collections.singletonList(command);
        }
        if (topLevelNode.kind() == SyntaxKind.FUNCTION_DEFINITION) {
//            if (ConfigurationHolder.getInstance()
//                    .isDocumentationCodeActionEnabled()) {
//                CodeAction codeAction = getAddDocsCodeAction.get(context, params);
//                codeActions.add(Either.forRight(codeAction));
//            }
        }

        return codeActions;
    }

    private static Node getTopLevelNode(Range range) {
        return null;
    }

    private static org.eclipse.lsp4j.Command getCreateVarCommand(Range range) {
        org.eclipse.lsp4j.Command command = new org.eclipse.lsp4j.Command();
        command.setCommand(Command.CREATE_VAR.getName());
        command.setTitle(Command.CREATE_VAR.getTitle());
        List<Object> args = new ArrayList<>();
        String typeDescriptor = getExpectedTypeDescriptor(range);
        args.add(new CommandArgument("type", typeDescriptor));
        command.setArguments(args);

        return command;
    }

    private static CodeAction getCreateVarCodeAction(BalCodeActionContext context, Range range, Diagnostic diagnostic,
                                                     CodeActionParams params) {
        CodeAction codeAction = new CodeAction();
        codeAction.setTitle(Command.CREATE_VAR.getTitle());
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
        return null;
    }

    private static Diagnostic getDiagnostic(Range range) {
        return null;
    }

    private static boolean getDocumentationConfig(BalCodeActionContext context)
            throws ExecutionException, InterruptedException {
        LanguageClient client = context.getClient();
        ConfigurationParams params = new ConfigurationParams();
        ConfigurationItem item = new ConfigurationItem();
        item.setSection("ballerina.codeAction.documentation");
        params.setItems(Collections.singletonList(item));
        CompletableFuture<List<Object>> configuration =
                client.configuration(params);
        List<Object> configValue = configuration.get();

        return (Boolean) (configValue.get(0));
    }
}
