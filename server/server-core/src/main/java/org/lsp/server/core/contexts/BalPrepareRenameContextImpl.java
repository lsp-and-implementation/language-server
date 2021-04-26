package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.PrepareRenameParams;
import org.lsp.server.api.context.LSContext;
import org.lsp.server.api.context.BalPrepareRenameContext;

public class BalPrepareRenameContextImpl extends BalPosBasedContextImpl implements BalPrepareRenameContext {

    public BalPrepareRenameContextImpl(LSContext serverContext, PrepareRenameParams params) {
        super(serverContext, params.getTextDocument().getUri(), params.getPosition());
    }
}
