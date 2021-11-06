package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.core.utils.CommonUtils;
import org.eclipse.lsp4j.DocumentSymbolParams;
import com.lspandimpl.server.api.context.BalDocumentSymbolContext;
import com.lspandimpl.server.api.context.LSContext;

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
