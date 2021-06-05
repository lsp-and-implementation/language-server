package org.lsp.server.api.context;

import org.eclipse.lsp4j.services.LanguageClient;

public interface BalCodeActionContext extends BalPosBasedContext {
    LanguageClient getClient();
}
