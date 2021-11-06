package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.BalDocumentColourContext;
import com.lspandimpl.server.api.context.LSContext;
import com.lspandimpl.server.core.utils.CommonUtils;
import org.eclipse.lsp4j.DocumentColorParams;

import java.nio.file.Path;

public class BalDocumentColourContextImpl extends BaseOperationContextImpl implements BalDocumentColourContext {
    private final DocumentColorParams params;

    public BalDocumentColourContextImpl(LSContext serverContext, DocumentColorParams params) {
        super(serverContext);
        this.params = params;
    }

    @Override
    public DocumentColorParams params() {
        return this.params;
    }

    @Override
    public Path getPath() {
        return CommonUtils.uriToPath(this.params.getTextDocument().getUri());
    }
}
