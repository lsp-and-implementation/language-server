package org.lsp.server.core;

import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpContext;
import org.lsp.server.api.context.BalSignatureContext;

public class SignatureProvider {
    public static SignatureHelp getSignatureHelp(BalSignatureContext context) {
        SignatureHelp signatureHelp = new SignatureHelp();
        SignatureHelpContext signatureHelpContext = context.signatureContext();
        
        if (signatureHelpContext.isRetrigger()) {
            /*
            If the trigger on top of a selected signature help
             */
//            return getSIgnatureHelpFromActiveSignature(context);
        }
        
        return signatureHelp;
    }
}
