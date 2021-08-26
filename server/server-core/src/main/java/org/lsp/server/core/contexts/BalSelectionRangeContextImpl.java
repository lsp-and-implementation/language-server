package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SelectionRangeParams;
import org.lsp.server.api.context.BalSelectionRangeContext;
import org.lsp.server.api.context.LSContext;

import java.util.List;

public class BalSelectionRangeContextImpl extends BalTextDocumentContextImpl implements BalSelectionRangeContext {
    private final SelectionRangeParams params;
    public BalSelectionRangeContextImpl(LSContext serverContext, SelectionRangeParams params) {
        super(serverContext, params.getTextDocument().getUri());
        this.params = params;
    }

    @Override
    public List<Position> positions() {
        return params.getPositions();
    }
}
