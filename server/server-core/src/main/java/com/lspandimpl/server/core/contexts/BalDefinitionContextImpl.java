package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.BalDefinitionContext;
import org.eclipse.lsp4j.Position;
import com.lspandimpl.server.api.context.LSContext;

public class BalDefinitionContextImpl extends BalPosBasedContextImpl implements BalDefinitionContext {
    public BalDefinitionContextImpl(LSContext serverContext, String uri, Position position) {
        super(serverContext, uri, position);
    }
}
