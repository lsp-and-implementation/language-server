package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.BalReferencesContext;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.ReferenceParams;
import com.lspandimpl.server.api.context.LSContext;

public class BalReferencesContextImpl extends BalPosBasedContextImpl implements BalReferencesContext {
    private final ReferenceParams params;

    public BalReferencesContextImpl(LSContext serverContext, ReferenceParams params) {
        super(serverContext, params.getTextDocument().getUri(), params.getPosition());
        this.params = params;
    }

    @Override
    public ReferenceContext getReferenceContext() {
        return this.params.getContext();
    }
}
