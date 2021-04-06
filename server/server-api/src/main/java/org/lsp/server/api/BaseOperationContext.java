package org.lsp.server.api;

import io.ballerina.projects.Document;
import org.lsp.server.ballerina.compiler.workspace.CompilerManager;

import java.util.Optional;

public interface BaseOperationContext {
    
    CompilerManager compilerManager();
    
    DiagnosticsPublisher diagnosticPublisher();

    Optional<Document> currentDocument();
    
    ClientLogManager clientLogManager();
}
