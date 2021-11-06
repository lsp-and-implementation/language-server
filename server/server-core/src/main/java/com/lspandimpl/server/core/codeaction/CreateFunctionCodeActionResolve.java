package com.lspandimpl.server.core.codeaction;

import com.google.gson.JsonObject;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.tools.text.LinePosition;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import com.lspandimpl.server.api.context.BalTextDocumentContext;

import java.util.Collections;

public class CreateFunctionCodeActionResolve {
    public static CodeAction getResolved(BalTextDocumentContext context, CodeAction unresolved) {
        JsonObject data = (JsonObject) unresolved.getData();
        String uri = data.get("uri").getAsString();
        String name = data.get("name").getAsString();

        WorkspaceEdit workspaceEdit = new WorkspaceEdit();
        TextDocumentEdit documentEdit = new TextDocumentEdit();
        TextEdit edit = new TextEdit();
        VersionedTextDocumentIdentifier textDocumentIdentifier = new VersionedTextDocumentIdentifier();
        // Passing data such as parameters and types, the server can generate a more detailed snippet
        String functionSignature = "function " + name + "() {" + System.lineSeparator() + "}";

        textDocumentIdentifier.setUri(uri);
        edit.setNewText(functionSignature);
        edit.setRange(getRange(context));
        documentEdit.setTextDocument(textDocumentIdentifier);
        documentEdit.setEdits(Collections.singletonList(edit));
        workspaceEdit.setDocumentChanges(Collections.singletonList(Either.forLeft(documentEdit)));
        unresolved.setEdit(workspaceEdit);

        return unresolved;
    }

    private static Range getRange(BalTextDocumentContext context) {
        LinePosition endLine = ((ModulePartNode) context.currentSyntaxTree().get().rootNode()).eofToken().lineRange().endLine();
        Position start = new Position(endLine.line(), endLine.offset());

        // In order to add the function to the end of the file,
        // we use the same position as the start and end of the range 
        return new Range(start, start);
    }
}
