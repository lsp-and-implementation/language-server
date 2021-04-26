package org.lsp.server.core.contexts;

import io.ballerina.projects.Document;
import org.eclipse.lsp4j.SignatureHelpContext;
import org.lsp.server.api.ClientLogManager;
import org.lsp.server.api.DiagnosticsPublisher;
import org.lsp.server.api.context.BalSignatureContext;
import org.lsp.server.ballerina.compiler.workspace.CompilerManager;

import java.util.Optional;

public class BalSignatureContextImpl implements BalSignatureContext {
    @Override
    public CompilerManager compilerManager() {
        return null;
    }

    @Override
    public DiagnosticsPublisher diagnosticPublisher() {
        return null;
    }

    @Override
    public Optional<Document> currentDocument() {
        return Optional.empty();
    }

    @Override
    public ClientLogManager clientLogManager() {
        return null;
    }

    @Override
    public SignatureHelpContext signatureContext() {
        return null;
    }
}
