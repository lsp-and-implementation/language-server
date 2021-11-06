package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.BalCodeActionContext;
import org.eclipse.lsp4j.Range;
import com.lspandimpl.server.api.context.LSContext;

public class BalCodeActionContextImpl extends BalPosBasedContextImpl implements BalCodeActionContext {
    
    private final Range range;
    public BalCodeActionContextImpl(LSContext serverContext, String uri, Range range) {
        super(serverContext, uri, range.getStart());
        this.range = range;
    }

    @Override
    public Range getRange() {
        return null;
    }
}
