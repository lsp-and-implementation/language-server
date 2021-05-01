package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.SemanticTokensParams;
import org.lsp.server.api.context.BalSemanticTokenContext;
import org.lsp.server.api.context.LSContext;
import org.lsp.server.core.utils.CommonUtils;

import java.nio.file.Path;

public class BalSemanticTokenContextImpl extends BaseOperationContextImpl implements BalSemanticTokenContext {
    private final SemanticTokensParams params;
    private LSContext serverContext;

    public BalSemanticTokenContextImpl(LSContext serverContext, SemanticTokensParams params) {
        super(serverContext);
        this.serverContext = serverContext;
        this.params = params;
    }

    @Override
    public SemanticTokensParams params() {
        return this.params;
    }

    @Override
    public Path getPath() {
        return CommonUtils.uriToPath(this.params.getTextDocument().getUri());
    }
}
