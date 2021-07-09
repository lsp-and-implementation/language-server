package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.lsp.server.api.context.BalCompletionContext;
import org.lsp.server.api.context.LSContext;

public class BalCompletionContextImpl extends BalPosBasedContextImpl implements BalCompletionContext {
    private final LSContext serverContext;

    public BalCompletionContextImpl(LSContext serverContext, CompletionParams params) {
        super(serverContext, params.getTextDocument().getUri(), params.getPosition());
        this.serverContext = serverContext;
    }
}
