package com.lspandimpl.server.api.context;

import org.eclipse.lsp4j.DocumentLinkParams;

import java.nio.file.Path;

public interface BalDocumentLinkContext extends BaseOperationContext {
    DocumentLinkParams params();

    /**
     * Get the current document path.
     *
     * @return {@link Path}
     */
    Path getPath();
}
