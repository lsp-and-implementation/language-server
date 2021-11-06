package com.lspandimpl.server.api.context;

import java.nio.file.Path;

public interface BalDocumentSymbolContext extends BaseOperationContext {
    /**
     * Get the current document path.
     *
     * @return {@link Path}
     */
    Path getPath();
}
