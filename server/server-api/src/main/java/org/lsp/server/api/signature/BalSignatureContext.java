package org.lsp.server.api.signature;

import org.eclipse.lsp4j.SignatureHelpContext;
import org.lsp.server.api.BaseOperationContext;

public interface BalSignatureContext extends BaseOperationContext {
    SignatureHelpContext signatureContext();
}
