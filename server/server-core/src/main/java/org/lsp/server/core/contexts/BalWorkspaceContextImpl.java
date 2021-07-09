package org.lsp.server.core.contexts;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.lsp.server.api.ClientLogManager;
import org.lsp.server.api.DiagnosticsPublisher;
import org.lsp.server.api.context.BalWorkspaceContext;
import org.lsp.server.api.context.LSContext;
import org.lsp.server.ballerina.compiler.workspace.CompilerManager;
import org.lsp.server.core.DiagnosticsPublisherImpl;
import org.lsp.server.core.compiler.manager.BallerinaCompilerManager;
import org.lsp.server.core.utils.ClientLogManagerImpl;

public class BalWorkspaceContextImpl implements BalWorkspaceContext {

    private final LSContext serverContext;

    public BalWorkspaceContextImpl(LSContext serverContext) {
        this.serverContext = serverContext;
    }

    @Override
    public CompilerManager compilerManager() {
        return BallerinaCompilerManager.getInstance(serverContext);
    }

    @Override
    public DiagnosticsPublisher diagnosticPublisher() {
        return DiagnosticsPublisherImpl.getInstance(serverContext);
    }

    @Override
    public ClientLogManager clientLogManager() {
        return ClientLogManagerImpl.getInstance(this.serverContext);
    }

    @Override
    public ClientCapabilities clientCapabilities() {
        return this.serverContext.getClientCapabilities().orElseThrow();
    }

    @Override
    public LanguageClient getClient() {
        return serverContext.getClient();
    }
}
