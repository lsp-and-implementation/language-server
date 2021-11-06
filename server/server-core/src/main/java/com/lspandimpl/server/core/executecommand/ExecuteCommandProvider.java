package com.lspandimpl.server.core.executecommand;

import com.lspandimpl.server.api.context.BalWorkspaceContext;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ExecuteCommandProvider {
    public static void executeCreateVarCommand(BalWorkspaceContext context,
                                               LanguageClient client,
                                               CreateVariableArgs args) {
        Map<String, List<TextEdit>> changes = new HashMap<>();

        TextEdit textEdit = new TextEdit();
        textEdit.setNewText(args.getNewText());
        textEdit.setRange(args.getRange());

        changes.put(args.getUri(), Collections.singletonList(textEdit));

        WorkspaceEdit edit = new WorkspaceEdit();
        edit.setChanges(changes);
        ApplyWorkspaceEditParams editParams = new ApplyWorkspaceEditParams();
        editParams.setEdit(edit);
        CompletableFuture<ApplyWorkspaceEditResponse> response =
                client.applyEdit(editParams);

    }
}
