package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.BalCompletionContext;
import org.eclipse.lsp4j.CompletionParams;
import com.lspandimpl.server.api.context.LSContext;

public class BalCompletionContextImpl extends BalPosBasedContextImpl implements BalCompletionContext {
    private final LSContext serverContext;

    public BalCompletionContextImpl(LSContext serverContext, CompletionParams params) {
        super(serverContext, params.getTextDocument().getUri(), params.getPosition());
        this.serverContext = serverContext;
    }
}
