package com.lspandimpl.server.api.context;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.eclipse.lsp4j.Position;

import java.util.List;

public interface BalPosBasedContext extends BalTextDocumentContext {
    List<Symbol> visibleSymbols();

    /**
     * Set the token at the completion's cursor position.
     *
     * @param token {@link Token} at the cursor
     */
    void setTokenAtCursor(Token token);

    /**
     * Get the token at the cursor.
     *
     * @return {@link Token}
     */
    Token getTokenAtCursor();

    /**
     * Set the node at cursor.
     *
     * @param node {@link NonTerminalNode} at the cursor position
     */
    void setNodeAtCursor(NonTerminalNode node);

    /**
     * Get the node at the completion request triggered cursor position.
     *
     * @return {@link NonTerminalNode} at the cursor position
     */
    NonTerminalNode getNodeAtCursor();

    /**
     * Set the cursor position as an offset value according to the syntax tree.
     *
     * @param offset of the cursor
     */
    void setCursorPositionInTree(int offset);

    /**
     * Get the cursor position as an offset value according to the syntax tree.
     *
     * @return {@link Integer} offset of the cursor
     */
    int getCursorPositionInTree();

    /**
     * Get the cursor position where the auto completion request triggered.
     *
     * @return {@link Position} cursor position
     */
    Position getCursorPosition();
}
