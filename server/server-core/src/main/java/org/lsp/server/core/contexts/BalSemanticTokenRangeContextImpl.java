package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.SemanticTokensRangeParams;
import org.lsp.server.api.context.BalSemanticTokenRangeContext;
import org.lsp.server.api.context.LSContext;
import org.lsp.server.core.utils.CommonUtils;

import java.nio.file.Path;

public class BalSemanticTokenRangeContextImpl extends BaseOperationContextImpl implements BalSemanticTokenRangeContext {
    private final SemanticTokensRangeParams params;
    private LSContext serverContext;

    public BalSemanticTokenRangeContextImpl(LSContext serverContext, SemanticTokensRangeParams params) {
        super(serverContext);
        this.serverContext = serverContext;
        this.params = params;
    }

    @Override
    public SemanticTokensRangeParams params() {
        return this.params;
    }

    @Override
    public Path getPath() {
        return CommonUtils.uriToPath(this.params.getTextDocument().getUri());
    }
}
