package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.Position;
import org.lsp.server.api.context.BalDefinitionContext;
import org.lsp.server.api.context.LSContext;

public class BalDefinitionContextImpl extends BalPosBasedContextImpl implements BalDefinitionContext {
    public BalDefinitionContextImpl(LSContext serverContext, String uri, Position position) {
        super(serverContext, uri, position);
    }
}
