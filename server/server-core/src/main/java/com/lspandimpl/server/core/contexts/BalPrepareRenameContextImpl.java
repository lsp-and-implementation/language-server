package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.BalPrepareRenameContext;
import com.lspandimpl.server.api.context.LSContext;
import org.eclipse.lsp4j.PrepareRenameParams;

public class BalPrepareRenameContextImpl extends BalPosBasedContextImpl implements BalPrepareRenameContext {

    public BalPrepareRenameContextImpl(LSContext serverContext, PrepareRenameParams params) {
        super(serverContext, params.getTextDocument().getUri(), params.getPosition());
    }
}
