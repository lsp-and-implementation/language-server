package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.SemanticTokensParams;
import org.lsp.server.api.context.BalSemanticTokenContext;
import org.lsp.server.api.context.LSContext;

public class BalSemanticTokenContextImpl extends BalTextDocumentContextImpl implements BalSemanticTokenContext {
    private final SemanticTokensParams params;

    public BalSemanticTokenContextImpl(LSContext serverContext, SemanticTokensParams params) {
        super(serverContext, params.getTextDocument().getUri());
        this.params = params;
    }

    @Override
    public SemanticTokensParams params() {
        return this.params;
    }
}
