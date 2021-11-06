package com.lspandimpl.server.api.context;

import org.eclipse.lsp4j.SemanticTokensDeltaParams;

import java.nio.file.Path;

public interface BalSemanticTokenDeltaContext extends BaseOperationContext {

    SemanticTokensDeltaParams params();

    /**
     * Get the current document path.
     *
     * @return {@link Path}
     */
    Path getPath();
}
