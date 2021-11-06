package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.BalDeclarationContext;
import org.eclipse.lsp4j.Position;
import com.lspandimpl.server.api.context.LSContext;

public class BalDeclarationContextImpl extends BalPosBasedContextImpl implements BalDeclarationContext {
    public BalDeclarationContextImpl(LSContext serverContext, String uri, Position position) {
        super(serverContext, uri, position);
    }
}
