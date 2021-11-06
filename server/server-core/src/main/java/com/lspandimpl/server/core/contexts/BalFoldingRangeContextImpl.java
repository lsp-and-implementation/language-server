package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.BalFoldingRangeContext;
import com.lspandimpl.server.core.utils.CommonUtils;
import org.eclipse.lsp4j.FoldingRangeRequestParams;
import com.lspandimpl.server.api.context.LSContext;

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
