package org.lsp.server.core.contexts;

import org.lsp.server.api.ClientLogManager;
import org.lsp.server.api.DiagnosticsPublisher;
import org.lsp.server.api.context.BaseOperationContext;
import org.lsp.server.api.context.LSContext;
import org.lsp.server.ballerina.compiler.workspace.CompilerManager;
import org.lsp.server.core.DiagnosticsPublisherImpl;
import org.lsp.server.core.compiler.manager.BallerinaCompilerManager;

public class BaseOperationContextImpl implements BaseOperationContext {
    private final LSContext serverContext;

    public BaseOperationContextImpl(LSContext serverContext) {
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
        return null;
    }
}
