package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.BalWorkspaceContext;
import com.lspandimpl.server.api.context.LSContext;

public class BalWorkspaceContextImpl extends BaseOperationContextImpl implements BalWorkspaceContext {
    public BalWorkspaceContextImpl(LSContext serverContext) {
        super(serverContext);
    }
}
