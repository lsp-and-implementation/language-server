package org.lsp.server.core.completion.utils;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.eclipse.lsp4j.Position;
import org.lsp.server.api.completion.BalCompletionContext;

import java.util.Optional;

public class ContextEvaluator {
    private ContextEvaluator() {
    }

    /**
     * Find the token at cursor.
     */
    public static void fillTokenInfoAtCursor(BalCompletionContext context) {
        context.setTokenAtCursor(findTokenAtPosition(context, context.getCursorPosition()));
        Optional<Document> document = context.currentDocument();
        if (document.isEmpty()) {
            throw new RuntimeException("Could not find a valid document");
        }
        TextDocument textDocument = document.get().textDocument();

        Position position = context.getCursorPosition();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        context.setCursorPositionInTree(txtPos);
        TextRange range = TextRange.from(txtPos, 0);
        NonTerminalNode nonTerminalNode = ((ModulePartNode) document.get().syntaxTree().rootNode()).findNode(range);

        while (true) {
            /*
            ModulePartNode's parent is null
             */
            if (nonTerminalNode.parent() != null && !withinTextRange(txtPos, nonTerminalNode)) {
                nonTerminalNode = nonTerminalNode.parent();
                continue;
            }
            break;
        }

        context.setNodeAtCursor(nonTerminalNode);
    }

    private static boolean withinTextRange(int position, NonTerminalNode node) {
        TextRange rangeWithMinutiae = node.textRangeWithMinutiae();
        TextRange textRange = node.textRange();
        TextRange leadingMinutiaeRange = TextRange.from(rangeWithMinutiae.startOffset(),
                textRange.startOffset() - rangeWithMinutiae.startOffset());
        return leadingMinutiaeRange.endOffset() <= position;
    }
    
    /**
     * Find the token at position.
     *
     * @return Token at position
     */
    public static Token findTokenAtPosition(BalCompletionContext context, Position position) {
        Optional<Document> document = context.currentDocument();
        if (document.isEmpty()) {
            throw new RuntimeException("Couldn't find a valid document!");
        }
        TextDocument textDocument = document.get().textDocument();

        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
        Token tokenAtPosition = ((ModulePartNode) document.get().syntaxTree().rootNode()).findToken(txtPos, true);

        if (tokenAtPosition == null) {
            throw new RuntimeException("Couldn't find a valid identifier token at position!");
        }

        return tokenAtPosition;
    }
}
