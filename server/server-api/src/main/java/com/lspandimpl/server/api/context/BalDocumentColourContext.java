package com.lspandimpl.server.api.context;

import org.eclipse.lsp4j.DocumentColorParams;

import java.nio.file.Path;

public interface BalDocumentColourContext extends BaseOperationContext {

    DocumentColorParams params();

    /**
     * Get the current document path.
     *
     * @return {@link Path}
     */
    Path getPath();
}
