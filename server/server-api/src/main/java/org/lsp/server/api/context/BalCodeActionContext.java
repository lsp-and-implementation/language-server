package org.lsp.server.api.context;

import org.eclipse.lsp4j.Range;

public interface BalCodeActionContext extends BalPosBasedContext {
    Range getRange();
}
