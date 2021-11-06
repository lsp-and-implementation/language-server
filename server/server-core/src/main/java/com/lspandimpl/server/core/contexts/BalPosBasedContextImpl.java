package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.DiagnosticsPublisher;
import com.lspandimpl.server.api.context.BalPosBasedContext;
import com.lspandimpl.server.api.context.LSContext;
import com.lspandimpl.server.ballerina.compiler.workspace.CompilerManager;
import com.lspandimpl.server.core.compiler.manager.BallerinaCompilerManager;
import com.lspandimpl.server.core.utils.CommonUtils;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import org.eclipse.lsp4j.Position;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BalPosBasedContextImpl extends BalTextDocumentContextImpl implements BalPosBasedContext {
    private final LSContext serverContext;
    private final String uri;
    private final Position position;
    private int cursor = -1;
    private NonTerminalNode nodeAtCursor;
    private Token tokenAtCursor;

    public BalPosBasedContextImpl(LSContext serverContext, String uri, Position position) {
        super(serverContext, uri);
        this.serverContext = serverContext;
        this.uri = uri;
        this.position = position;
    }

    @Override
    public CompilerManager compilerManager() {
        return BallerinaCompilerManager.getInstance(this.serverContext);
    }

    @Override
    public DiagnosticsPublisher diagnosticPublisher() {
        return null;
    }

    @Override
    public List<Symbol> visibleSymbols() {
        Path path = CommonUtils.uriToPath(uri);
        Document currentDoc = this.currentDocument().orElseThrow();
        Position cursorPosition = this.getCursorPosition();
        LinePosition linePosition = LinePosition.from(cursorPosition.getLine(), cursorPosition.getCharacter());
        Optional<SemanticModel> semanticModel = BallerinaCompilerManager.getInstance(this.serverContext)
                .getSemanticModel(path);
        if (semanticModel.isEmpty()) {
            return Collections.emptyList();
        }

        return semanticModel.get().visibleSymbols(currentDoc, linePosition);
    }

    @Override
    public void setTokenAtCursor(Token token) {
        this.tokenAtCursor = token;
    }

    @Override
    public Token getTokenAtCursor() {
        if (this.tokenAtCursor == null) {
            throw new RuntimeException("Token at cursor is not set");
        }

        return this.tokenAtCursor;
    }

    @Override
    public void setNodeAtCursor(NonTerminalNode node) {
        this.nodeAtCursor = node;
    }

    @Override
    public NonTerminalNode getNodeAtCursor() {
        if (nodeAtCursor == null) {
            throw new RuntimeException("Node at cursor is not set");
        }

        return this.nodeAtCursor;
    }

    @Override
    public void setCursorPositionInTree(int offset) {
        if (this.cursor > 0) {
            throw new RuntimeException("Cursor cannot set more than once");
        }

        this.cursor = offset;
    }

    @Override
    public int getCursorPositionInTree() {
        if (this.cursor == -1) {
            throw new RuntimeException("Cursor is not set");
        }

        return this.cursor;
    }

    @Override
    public Position getCursorPosition() {
        return position;
    }
}
