package org.lsp.server.api.context;

import org.lsp.server.api.ClientLogManager;
import org.lsp.server.api.DiagnosticsPublisher;
import org.lsp.server.ballerina.compiler.workspace.CompilerManager;

public interface BaseOperationContext {

    CompilerManager compilerManager();

    DiagnosticsPublisher diagnosticPublisher();

    ClientLogManager clientLogManager();
}
