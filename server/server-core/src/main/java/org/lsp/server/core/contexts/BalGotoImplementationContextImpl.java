package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.Position;
import org.lsp.server.api.context.BalGotoImplContext;
import org.lsp.server.api.context.BalTypeDefContext;
import org.lsp.server.api.context.LSContext;

public class BalGotoImplementationContextImpl extends BalPosBasedContextImpl implements BalGotoImplContext {
    public BalGotoImplementationContextImpl(LSContext serverContext, String uri, Position position) {
        super(serverContext, uri, position);
    }
}
