package org.lsp.server.core.contexts;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Document;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.lsp.server.api.ClientLogManager;
import org.lsp.server.api.DiagnosticsPublisher;
import org.lsp.server.api.LSContext;
import org.lsp.server.api.completion.BalCompletionResolveContext;
import org.lsp.server.ballerina.compiler.workspace.CompilerManager;
import org.lsp.server.core.compiler.manager.BallerinaCompilerManager;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BalCompletionResolveContextImpl implements BalCompletionResolveContext {
    private final LSContext serverContext;
    private final CompletionItem unresolved;
    private final CompletionCapabilities completionCapabilities;

    public BalCompletionResolveContextImpl(LSContext serverContext, CompletionItem unresolved) {
        this.serverContext = serverContext;
        this.unresolved = unresolved;
        this.completionCapabilities = serverContext.getClientCapabilities().get().getTextDocument().getCompletion();
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
    public Optional<Document> currentDocument() {
        return Optional.empty();
//        Path path = CommonUtils.uriToPath(this.params.getTextDocument().getUri());
//        return BallerinaCompilerManager.getInstance(this.serverContext).getDocument(path);
    }

    @Override
    public ClientLogManager clientLogManager() {
        return null;
    }

    @Override
    public List<Symbol> visibleSymbols() {
//        Path path = CommonUtils.uriToPath(this.params.getTextDocument().getUri());
//        Document currentDoc = this.currentDocument().orElseThrow();
//        Position cursorPosition = this.getCursorPosition();
//        LinePosition linePosition = LinePosition.from(cursorPosition.getLine(), cursorPosition.getCharacter());
//        Optional<SemanticModel> semanticModel = BallerinaCompilerManager.getInstance(this.serverContext)
//                .getSemanticModel(path);
//        if (semanticModel.isEmpty()) {
        return Collections.emptyList();
//        }
//        
//        return semanticModel.get().visibleSymbols(currentDoc, linePosition);
    }

    @Override
    public CompletionItem unresolved() {
        return this.unresolved;
    }

    @Override
    public CompletionCapabilities clientCapabilities() {
        return this.completionCapabilities;
    }

    @Override
    public Position getCursorPosition() {
        return null;
    }
}
