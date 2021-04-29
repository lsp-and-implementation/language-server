package org.lsp.server.api.context;

import org.eclipse.lsp4j.DocumentLinkCapabilities;
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
    
    DocumentLinkCapabilities clientCapabilities();
}
