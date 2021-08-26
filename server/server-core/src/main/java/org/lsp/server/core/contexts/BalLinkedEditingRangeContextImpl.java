package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.Position;
import org.lsp.server.api.context.BalLinkedEditingRangeContext;
import org.lsp.server.api.context.LSContext;

public class BalLinkedEditingRangeContextImpl extends BalPosBasedContextImpl implements BalLinkedEditingRangeContext {
    public BalLinkedEditingRangeContextImpl(LSContext serverContext, String uri, Position position) {
        super(serverContext, uri, position);
    }
}
