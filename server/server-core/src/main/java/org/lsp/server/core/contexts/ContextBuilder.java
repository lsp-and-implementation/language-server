package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.PrepareRenameParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SemanticTokensDeltaParams;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SemanticTokensRangeParams;
import org.lsp.server.api.context.BalCompletionContext;
import org.lsp.server.api.context.BalCompletionResolveContext;
import org.lsp.server.api.context.BalDocumentHighlightContext;
import org.lsp.server.api.context.BalDocumentSymbolContext;
import org.lsp.server.api.context.BalPrepareRenameContext;
import org.lsp.server.api.context.BalRenameContext;
import org.lsp.server.api.context.BalSemanticTokenContext;
import org.lsp.server.api.context.BalSemanticTokenDeltaContext;
import org.lsp.server.api.context.BalSemanticTokenRangeContext;
import org.lsp.server.api.context.BaseOperationContext;
import org.lsp.server.api.context.LSContext;

public class ContextBuilder {
    public static BaseOperationContext baseContext(LSContext serverContext) {
        return new BaseOperationContextImpl(serverContext);
    }

    public static BalCompletionContext completionContext(LSContext serverContext, CompletionParams params) {
        return new BalCompletionContextImpl(serverContext, params);
    }

    public static BalCompletionResolveContext completionResolveContext(LSContext serverContext,
                                                                       CompletionItem unresolved) {
        return new BalCompletionResolveContextImpl(serverContext, unresolved);
    }

    public static BalRenameContext renameContext(LSContext serverContext, RenameParams params) {
        return new BalRenameContextImpl(serverContext, params);
    }

    public static BalPrepareRenameContext prepareRenameContext(LSContext serverContext, PrepareRenameParams params) {
        return new BalPrepareRenameContextImpl(serverContext, params);
    }

    public static BalDocumentSymbolContext documentSymbolContext(LSContext context, DocumentSymbolParams params) {
        return new BalDocumentSymbolContextImpl(context, params);
    }

    public static BalDocumentHighlightContext documentHighlightContext(LSContext context,
                                                                       DocumentHighlightParams params) {
        return new BalDocumentHighlightContextImpl(context, params);
    }

    public static BalSemanticTokenContext semanticTokensContext(LSContext serverContext, SemanticTokensParams params) {
        return new BalSemanticTokenContextImpl(serverContext, params);
    }

    public static BalSemanticTokenRangeContext semanticTokensRangeContext(LSContext serverContext, SemanticTokensRangeParams params) {
        return null;
    }

    public static BalSemanticTokenDeltaContext semanticTokensDeltaContext(LSContext serverContext, SemanticTokensDeltaParams params) {
        return null;
    }
}
