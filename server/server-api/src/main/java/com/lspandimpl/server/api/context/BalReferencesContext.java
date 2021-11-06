package com.lspandimpl.server.api.context;

import org.eclipse.lsp4j.ReferenceContext;

public interface BalReferencesContext extends BalPosBasedContext {
    ReferenceContext getReferenceContext();
}
