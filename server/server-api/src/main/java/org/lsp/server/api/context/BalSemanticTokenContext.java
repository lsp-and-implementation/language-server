package org.lsp.server.api.context;

import org.eclipse.lsp4j.SemanticTokensParams;

public interface BalSemanticTokenContext extends BalTextDocumentContext {
    SemanticTokensParams params();
}
