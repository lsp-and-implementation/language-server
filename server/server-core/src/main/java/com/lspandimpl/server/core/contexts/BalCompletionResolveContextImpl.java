package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.core.compiler.manager.BallerinaCompilerManager;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.projects.Document;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import com.lspandimpl.server.api.DiagnosticsPublisher;
import com.lspandimpl.server.api.context.LSContext;
import com.lspandimpl.server.api.context.BalCompletionResolveContext;
import com.lspandimpl.server.ballerina.compiler.workspace.CompilerManager;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BalCompletionResolveContextImpl extends BalTextDocumentContextImpl implements BalCompletionResolveContext {
    private final LSContext serverContext;
    private final CompletionItem unresolved;

    public BalCompletionResolveContextImpl(LSContext serverContext, CompletionItem unresolved) {
        super(serverContext, null);
        this.serverContext = serverContext;
        this.unresolved = unresolved;
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
    public Position getCursorPosition() {
        return null;
    }
}
