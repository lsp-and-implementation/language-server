package org.lsp.server.api.context;

import org.eclipse.lsp4j.ReferenceContext;

public interface BalReferencesContext extends BalPosBasedContext {
    ReferenceContext getReferenceContext();
}
