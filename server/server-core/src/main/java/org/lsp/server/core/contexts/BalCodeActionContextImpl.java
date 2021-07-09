package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.lsp.server.api.context.BalCodeActionContext;
import org.lsp.server.api.context.LSContext;

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
