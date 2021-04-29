package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.DocumentHighlightParams;
import org.lsp.server.api.context.BalDocumentHighlightContext;
import org.lsp.server.api.context.LSContext;

public class BalDocumentHighlightContextImpl extends BalPosBasedContextImpl implements BalDocumentHighlightContext {
    public BalDocumentHighlightContextImpl(LSContext context, DocumentHighlightParams params) {
        super(context, params.getTextDocument().getUri(), params.getPosition());
    }
}
