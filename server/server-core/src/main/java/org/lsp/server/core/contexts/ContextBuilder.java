package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;
import org.lsp.server.api.LSContext;
import org.lsp.server.api.BaseOperationContext;
import org.lsp.server.api.completion.BalCompletionContext;
import org.lsp.server.api.completion.BalCompletionResolveContext;

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
}
