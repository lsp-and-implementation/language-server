package org.lsp.server.api.context;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.lsp.server.api.ClientLogManager;
import org.lsp.server.api.DiagnosticsPublisher;
import org.lsp.server.ballerina.compiler.workspace.CompilerManager;

public interface BaseOperationContext {

    CompilerManager compilerManager();

    DiagnosticsPublisher diagnosticPublisher();

    ClientLogManager clientLogManager();
    
    ClientCapabilities clientCapabilities();
    
    LanguageClient getClient();
}
