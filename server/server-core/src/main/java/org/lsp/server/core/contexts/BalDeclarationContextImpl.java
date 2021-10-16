package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.Position;
import org.lsp.server.api.context.BalDeclarationContext;
import org.lsp.server.api.context.LSContext;

public class BalDeclarationContextImpl extends BalPosBasedContextImpl implements BalDeclarationContext {
    public BalDeclarationContextImpl(LSContext serverContext, String uri, Position position) {
        super(serverContext, uri, position);
    }
}
