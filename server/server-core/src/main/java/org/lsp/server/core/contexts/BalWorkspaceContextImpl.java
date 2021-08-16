package org.lsp.server.core.contexts;

import org.lsp.server.api.context.BalWorkspaceContext;
import org.lsp.server.api.context.LSContext;

public class BalWorkspaceContextImpl extends BaseOperationContextImpl implements BalWorkspaceContext {
    public BalWorkspaceContextImpl(LSContext serverContext) {
        super(serverContext);
    }
}
