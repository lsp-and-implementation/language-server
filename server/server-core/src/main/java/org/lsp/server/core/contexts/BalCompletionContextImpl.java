package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionParams;
import org.lsp.server.api.context.LSContext;
import org.lsp.server.api.context.BalCompletionContext;

public class BalCompletionContextImpl extends BalPosBasedContextImpl implements BalCompletionContext {
    private final LSContext serverContext;

    public BalCompletionContextImpl(LSContext serverContext, CompletionParams params) {
        super(serverContext, params.getTextDocument().getUri(), params.getPosition());
        this.serverContext = serverContext;
    }
}
