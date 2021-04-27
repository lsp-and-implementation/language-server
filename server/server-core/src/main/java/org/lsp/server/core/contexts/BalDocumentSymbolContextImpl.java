package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.DocumentSymbolParams;
import org.lsp.server.api.context.BalDocumentSymbolContext;
import org.lsp.server.api.context.LSContext;
import org.lsp.server.core.utils.CommonUtils;

import java.nio.file.Path;

public class BalDocumentSymbolContextImpl extends BaseOperationContextImpl implements BalDocumentSymbolContext {
    
    private final DocumentSymbolParams params;
    
    public BalDocumentSymbolContextImpl(LSContext serverContext, DocumentSymbolParams params) {
        super(serverContext);
        this.params = params;
    }

    @Override
    public Path getPath() {
        return CommonUtils.uriToPath(this.params.getTextDocument().getUri());
    }
}
