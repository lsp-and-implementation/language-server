package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.DocumentLinkCapabilities;
import org.eclipse.lsp4j.DocumentLinkParams;
import org.lsp.server.api.context.BalDocumentLinkContext;
import org.lsp.server.api.context.LSContext;
import org.lsp.server.core.utils.CommonUtils;

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

    @Override
    public DocumentLinkCapabilities clientCapabilities() {
        return this.serverContext.getClientCapabilities().orElseThrow().getTextDocument().getDocumentLink();
    }
}
