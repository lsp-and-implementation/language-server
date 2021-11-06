package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.BalLinkedEditingRangeContext;
import org.eclipse.lsp4j.Position;
import com.lspandimpl.server.api.context.LSContext;

public class BalLinkedEditingRangeContextImpl extends BalPosBasedContextImpl implements BalLinkedEditingRangeContext {
    public BalLinkedEditingRangeContextImpl(LSContext serverContext, String uri, Position position) {
        super(serverContext, uri, position);
    }
}
