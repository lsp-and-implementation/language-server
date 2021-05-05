package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.FoldingRangeRequestParams;
import org.lsp.server.api.context.BalFoldingRangeContext;
import org.lsp.server.api.context.LSContext;
import org.lsp.server.core.utils.CommonUtils;

import java.nio.file.Path;

public class BalFoldingRangeContextImpl extends BaseOperationContextImpl implements BalFoldingRangeContext {
    private final FoldingRangeRequestParams params;

    public BalFoldingRangeContextImpl(LSContext serverContext, FoldingRangeRequestParams params) {
        super(serverContext);
        this.params = params;
    }

    @Override
    public Path getPath() {
        return CommonUtils.uriToPath(this.params.getTextDocument().getUri());
    }
}
