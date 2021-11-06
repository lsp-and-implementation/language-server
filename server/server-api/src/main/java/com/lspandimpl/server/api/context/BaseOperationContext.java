package com.lspandimpl.server.api.context;

import com.lspandimpl.server.api.ConfigurationHolder;
import com.lspandimpl.server.ballerina.compiler.workspace.CompilerManager;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import com.lspandimpl.server.api.ClientLogManager;
import com.lspandimpl.server.api.DiagnosticsPublisher;

public interface BaseOperationContext {

    CompilerManager compilerManager();

    DiagnosticsPublisher diagnosticPublisher();

    ConfigurationHolder clientConfigHolder();

    ClientLogManager clientLogManager();
    
    ClientCapabilities clientCapabilities();
    
    LanguageClient getClient();
}
