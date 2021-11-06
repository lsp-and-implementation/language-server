package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.BalSemanticTokenDeltaContext;
import com.lspandimpl.server.core.utils.CommonUtils;
import org.eclipse.lsp4j.SemanticTokensDeltaParams;
import com.lspandimpl.server.api.context.LSContext;

import java.nio.file.Path;

public class BalSemanticTokenDeltaContextImpl extends BaseOperationContextImpl implements BalSemanticTokenDeltaContext {
    private final SemanticTokensDeltaParams params;
    private LSContext serverContext;

    public BalSemanticTokenDeltaContextImpl(LSContext serverContext, SemanticTokensDeltaParams params) {
        super(serverContext);
        this.serverContext = serverContext;
        this.params = params;
    }

    @Override
    public SemanticTokensDeltaParams params() {
        return this.params;
    }

    @Override
    public Path getPath() {
        return CommonUtils.uriToPath(this.params.getTextDocument().getUri());
    }
}
