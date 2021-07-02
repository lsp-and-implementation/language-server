package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.Position;
import org.lsp.server.api.context.BalHoverContext;
import org.lsp.server.api.context.LSContext;

public class BalHoverContextImpl extends BalPosBasedContextImpl implements BalHoverContext {
    public BalHoverContextImpl(LSContext serverContext, String uri, Position position) {
        super(serverContext, uri, position);
    }
}
