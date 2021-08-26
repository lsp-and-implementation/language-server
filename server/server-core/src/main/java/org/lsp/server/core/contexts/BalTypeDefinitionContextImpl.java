package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.Position;
import org.lsp.server.api.context.BalTypeDefContext;
import org.lsp.server.api.context.LSContext;

public class BalTypeDefinitionContextImpl extends BalPosBasedContextImpl implements BalTypeDefContext {
    public BalTypeDefinitionContextImpl(LSContext serverContext, String uri, Position position) {
        super(serverContext, uri, position);
    }
}
