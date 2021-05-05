package org.lsp.server.api.context;

import java.nio.file.Path;

public interface BalFoldingRangeContext extends BaseOperationContext {

    /**
     * Get the current document path.
     *
     * @return {@link Path}
     */
    Path getPath();
}
