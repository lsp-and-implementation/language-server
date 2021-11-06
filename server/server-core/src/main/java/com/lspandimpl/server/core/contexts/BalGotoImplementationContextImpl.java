package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.BalGotoImplContext;
import com.lspandimpl.server.api.context.LSContext;
import org.eclipse.lsp4j.Position;

public class BalGotoImplementationContextImpl extends BalPosBasedContextImpl implements BalGotoImplContext {
    public BalGotoImplementationContextImpl(LSContext serverContext, String uri, Position position) {
        super(serverContext, uri, position);
    }
}
