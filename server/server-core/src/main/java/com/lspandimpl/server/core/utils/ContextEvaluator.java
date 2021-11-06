package com.lspandimpl.server.core.utils;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.eclipse.lsp4j.Position;
import com.lspandimpl.server.api.context.BalPosBasedContext;
import com.lspandimpl.server.api.context.BalTextDocumentContext;

import java.util.Optional;

public class ContextEvaluator {
    private ContextEvaluator() {
    }

    /**
     * Find the token at cursor.
     */
    public static void fillTokenInfoAtCursor(BalPosBasedContext context) {
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
    
    public static NonTerminalNode nodeAtPosition(Position position, BalTextDocumentContext context) {
        Optional<Document> document = context.currentDocument();
        if (document.isEmpty()) {
            throw new RuntimeException("Could not find a valid document");
        }
        TextDocument textDocument = document.get().textDocument();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(position.getLine(), position.getCharacter()));
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
        
        return nonTerminalNode;
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
    private static Token findTokenAtPosition(BalPosBasedContext context, Position position) {
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
