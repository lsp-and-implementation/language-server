package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.DocumentColorParams;
import org.eclipse.lsp4j.DocumentLinkCapabilities;
import org.eclipse.lsp4j.DocumentLinkParams;
import org.lsp.server.api.context.BalDocumentColourContext;
import org.lsp.server.api.context.BalDocumentLinkContext;
import org.lsp.server.api.context.LSContext;
import org.lsp.server.core.utils.CommonUtils;

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
