package com.lspandimpl.server.core.contexts;

import org.eclipse.lsp4j.DocumentHighlightParams;
import com.lspandimpl.server.api.context.BalDocumentHighlightContext;
import com.lspandimpl.server.api.context.LSContext;

public class BalDocumentHighlightContextImpl extends BalPosBasedContextImpl implements BalDocumentHighlightContext {
    public BalDocumentHighlightContextImpl(LSContext context, DocumentHighlightParams params) {
        super(context, params.getTextDocument().getUri(), params.getPosition());
    }
}
