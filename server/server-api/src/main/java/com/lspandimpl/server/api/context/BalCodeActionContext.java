package com.lspandimpl.server.api.context;

import org.eclipse.lsp4j.Range;

public interface BalCodeActionContext extends BalPosBasedContext {
    Range getRange();
}
