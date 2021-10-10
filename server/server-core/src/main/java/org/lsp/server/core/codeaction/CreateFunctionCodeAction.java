package org.lsp.server.core.codeaction;

import com.google.gson.JsonObject;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.lsp.server.api.context.BalCodeActionContext;

public class CreateFunctionCodeAction {
    public static CodeAction getCodeAction(BalCodeActionContext context,
                                           Diagnostic diagnostic,
                                           CodeActionParams params) {
        CodeAction codeAction = new CodeAction();
        codeAction.setTitle(BalCommand.CREATE_FUNCTION.getTitle());
        String funcName = diagnostic.message()
                .replace("undefined function", "")
                .replace("'", "").trim();
        int eLine = context.currentDocument().get().syntaxTree().rootNode().lineRange().endLine().line();

        JsonObject data = new JsonObject();
        data.addProperty("uri", params.getTextDocument().getUri());
        data.addProperty("name", funcName);
        codeAction.setData(data);
        
        return codeAction;
    }
}
