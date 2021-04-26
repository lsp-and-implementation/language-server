package org.lsp.server.api.context;

import org.eclipse.lsp4j.CompletionCapabilities;

public interface BalCompletionContext extends BalPosBasedContext {
    CompletionCapabilities clientCapabilities();
}
