package com.lspandimpl.server.api.context;

import org.eclipse.lsp4j.SemanticTokensRangeParams;

import java.nio.file.Path;

public interface BalSemanticTokenRangeContext extends BaseOperationContext {

    SemanticTokensRangeParams params();

    /**
     * Get the current document path.
     *
     * @return {@link Path}
     */
    Path getPath();
}
