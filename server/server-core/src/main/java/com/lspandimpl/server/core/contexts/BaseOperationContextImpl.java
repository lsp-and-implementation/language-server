package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.ConfigurationHolder;
import com.lspandimpl.server.ballerina.compiler.workspace.CompilerManager;
import com.lspandimpl.server.core.DiagnosticsPublisherImpl;
import com.lspandimpl.server.core.compiler.manager.BallerinaCompilerManager;
import com.lspandimpl.server.core.utils.ClientLogManagerImpl;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import com.lspandimpl.server.api.ClientLogManager;
import com.lspandimpl.server.api.DiagnosticsPublisher;
import com.lspandimpl.server.api.context.BaseOperationContext;
import com.lspandimpl.server.api.context.LSContext;
import com.lspandimpl.server.core.configdidchange.ConfigurationHolderImpl;

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
        return ClientLogManagerImpl.getInstance(this.serverContext);
    }

    @Override
    public ClientCapabilities clientCapabilities() {
        return serverContext.getClientCapabilities().orElseThrow();
    }
    
    @Override
    public LanguageClient getClient() {
        return this.serverContext.getClient();
    }

    @Override
    public ConfigurationHolder clientConfigHolder() {
        return ConfigurationHolderImpl.getInstance(serverContext);
    }
}
