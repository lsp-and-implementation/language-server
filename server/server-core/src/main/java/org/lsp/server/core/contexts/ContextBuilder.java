package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.PrepareRenameParams;
import org.eclipse.lsp4j.RenameParams;
import org.lsp.server.api.context.BalCompletionContext;
import org.lsp.server.api.context.BalCompletionResolveContext;
import org.lsp.server.api.context.BalDocumentSymbolContext;
import org.lsp.server.api.context.BalPrepareRenameContext;
import org.lsp.server.api.context.BalRenameContext;
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
}
