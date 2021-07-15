package org.lsp.server.core.contexts;

import org.lsp.server.api.context.BalCodeLensContext;
import org.lsp.server.api.context.LSContext;

public class BalCodeLensContextImpl extends BalTextDocumentContextImpl implements BalCodeLensContext {
    public BalCodeLensContextImpl(LSContext serverContext, String uri) {
        super(serverContext, uri);
    }
}
