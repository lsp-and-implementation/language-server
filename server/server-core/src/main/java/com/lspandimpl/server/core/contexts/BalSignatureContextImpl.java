package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.BalSignatureContext;
import org.eclipse.lsp4j.SignatureHelpContext;
import org.eclipse.lsp4j.SignatureHelpParams;
import com.lspandimpl.server.api.context.LSContext;

public class BalSignatureContextImpl extends BalPosBasedContextImpl implements BalSignatureContext {
    private final SignatureHelpParams params;

    public BalSignatureContextImpl(LSContext serverContext, SignatureHelpParams params) {
        super(serverContext, params.getTextDocument().getUri(), params.getPosition());
        this.params = params;
    }

    @Override
    public SignatureHelpContext signatureContext() {
        return null;
    }

    @Override
    public int activeParameter() {
        return 0;
    }

    @Override
    public SignatureHelpParams getParams() {
        return this.params;
    }
}
