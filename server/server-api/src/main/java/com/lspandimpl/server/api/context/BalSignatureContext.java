package com.lspandimpl.server.api.context;

import org.eclipse.lsp4j.SignatureHelpContext;
import org.eclipse.lsp4j.SignatureHelpParams;

public interface BalSignatureContext extends BalPosBasedContext {
    SignatureHelpContext signatureContext();
    
    int activeParameter();
    
    SignatureHelpParams getParams();
}
