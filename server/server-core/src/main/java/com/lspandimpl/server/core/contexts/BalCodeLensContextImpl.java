package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.BalCodeLensContext;
import com.lspandimpl.server.api.context.LSContext;

public class BalCodeLensContextImpl extends BalTextDocumentContextImpl implements BalCodeLensContext {
    public BalCodeLensContextImpl(LSContext serverContext, String uri) {
        super(serverContext, uri);
    }
}
