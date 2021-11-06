package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.BalDocumentLinkContext;
import com.lspandimpl.server.core.utils.CommonUtils;
import org.eclipse.lsp4j.DocumentLinkParams;
import com.lspandimpl.server.api.context.LSContext;

import java.nio.file.Path;

public class BalDocumentLinkContextImpl extends BaseOperationContextImpl implements BalDocumentLinkContext {
    private final DocumentLinkParams params;
    private final LSContext serverContext;

    public BalDocumentLinkContextImpl(LSContext serverContext, DocumentLinkParams params) {
        super(serverContext);
        this.serverContext = serverContext;
        this.params = params;
    }

    @Override
    public DocumentLinkParams params() {
        return this.params;
    }

    @Override
    public Path getPath() {
        return CommonUtils.uriToPath(this.params.getTextDocument().getUri());
    }
}
