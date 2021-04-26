package org.lsp.server.core.rename;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Document;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import org.eclipse.lsp4j.ChangeAnnotation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PrepareRenameResult;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.lsp.server.api.context.BalPrepareRenameContext;
import org.lsp.server.api.context.BalRenameContext;
import org.lsp.server.ballerina.compiler.workspace.CompilerManager;
import org.lsp.server.core.utils.CommonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RenameProvider {
    public static WorkspaceEdit
    getRename(BalRenameContext context) {
        String newName = context.params().getNewName();
        if (CommonUtils.isKeyword(newName)) {
            context.clientLogManager()
                    .showErrorMessage("Trying to rename" +
                            " to a reserved keyword");
            return null;
        }

        CompilerManager compilerManager = context.compilerManager();
        Position cursor = context.getCursorPosition();
        Optional<SemanticModel> semanticModel =
                compilerManager.getSemanticModel(context.getPath());
        Document document = compilerManager
                .getDocument(context.getPath()).orElseThrow();
        LinePosition linePosition = LinePosition
                .from(cursor.getLine(), cursor.getCharacter());
        List<Location> references = semanticModel.orElseThrow()
                .references(document, linePosition);

        WorkspaceEdit workspaceEdit = new WorkspaceEdit();
        workspaceEdit.setChanges(getChanges(context, references));
        // set the change annotations
        Map<String, ChangeAnnotation> changeAnnotations =
                getChangeAnnotations(context, references);
        workspaceEdit.setChangeAnnotations(changeAnnotations);

        return workspaceEdit;
    }

    public static PrepareRenameResult
    prepareRename(BalPrepareRenameContext context) {
        Token tokenAtCursor = context.getTokenAtCursor();
        if (tokenAtCursor.kind() != SyntaxKind.IDENTIFIER_TOKEN
                || CommonUtils.isKeyword(tokenAtCursor.text())) {
            return null;
        }

        LinePosition startLine = tokenAtCursor.lineRange().startLine();
        LinePosition endLine = tokenAtCursor.lineRange().endLine();

        PrepareRenameResult renameResult = new PrepareRenameResult();
        Range range = new Range();
        range.setStart(new Position(startLine.line(), startLine.offset()));
        range.setEnd(new Position(endLine.line(), endLine.offset()));
        renameResult.setPlaceholder("renamed" + tokenAtCursor.text());
        renameResult.setRange(range);

        return renameResult;
    }

    private static Map<String, ChangeAnnotation> getChangeAnnotations(BalRenameContext context, List<Location> references) {
        return null;
    }

    private static Map<String, List<TextEdit>> getChanges(BalRenameContext context, List<Location> references) {
        return new HashMap<>();
    }
}
