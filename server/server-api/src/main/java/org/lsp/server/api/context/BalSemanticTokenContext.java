package org.lsp.server.api.context;

import org.eclipse.lsp4j.SemanticTokensParams;

import java.nio.file.Path;

public interface BalSemanticTokenContext extends BaseOperationContext {

    SemanticTokensParams params();

    /**
     * Get the current document path.
     *
     * @return {@link Path}
     */
    Path getPath();
}
