package org.lsp.server.api.context;

import org.eclipse.lsp4j.SignatureHelpContext;

public interface BalSignatureContext extends BaseOperationContext {
    SignatureHelpContext signatureContext();
}
