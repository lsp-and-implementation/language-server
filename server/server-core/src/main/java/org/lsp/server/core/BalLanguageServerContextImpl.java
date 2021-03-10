/*
 * Copyright (c) 2021, Nadeeshaan Gunasinghe, Nipuna Marcus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lsp.server.core;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.lsp.server.api.BalLanguageServerContext;
import org.lsp.server.api.ClientLogManager;
import org.lsp.server.api.DiagnosticsPublisher;
import org.lsp.server.ballerina.compiler.workspace.CompilerManager;
import org.lsp.server.core.compiler.manager.BallerinaCompilerManager;
import org.lsp.server.core.utils.ClientLogManagerImpl;

import java.util.Optional;

/**
 * Ballerina language server context implementation.
 * 
 * @since 1.0.0
 */
public class BalLanguageServerContextImpl implements BalLanguageServerContext {
    private ClientCapabilities clientCapabilities;
    private DiagnosticsPublisher diagnosticsPublisherImpl;
    private LanguageClient languageClient;
    private ClientLogManager logManager;
    private CompilerManager compilerManager;
    private boolean initialized = false;

    @Override
    public Optional<ClientCapabilities> clientCapabilities() {
        return Optional.ofNullable(this.clientCapabilities);
    }

    @Override
    public ClientLogManager logManager() {
        if (!this.initialized) {
            throw new RuntimeException("Context not initialized yet");
        }

        if (this.logManager == null) {
            this.logManager = new ClientLogManagerImpl(this.languageClient);
        }
        return null;
    }

    /**
     * Set the client capabilities.
     * 
     * @param clientCapabilities {@link ClientCapabilities}
     */
    public void setClientCapabilities(ClientCapabilities clientCapabilities) {
        this.clientCapabilities = clientCapabilities;
    }

    @Override
    public DiagnosticsPublisher diagnosticsPublisher() {
        if (!this.initialized) {
            throw new RuntimeException("Context not initialized yet");
        }
        
        if (this.diagnosticsPublisherImpl == null) {
            this.diagnosticsPublisherImpl = new DiagnosticsPublisherImpl(this.languageClient);
        }
        
        return this.diagnosticsPublisherImpl;
    }
    
    @Override
    public LanguageClient languageClient() {
        if (!this.initialized) {
            throw new RuntimeException("Context not initialized yet");
        }
        return languageClient;
    }

    @Override
    public CompilerManager compilerManager() {
        if (!this.initialized) {
            throw new RuntimeException("Context not initialized yet");
        }
        if (this.compilerManager == null) {
            this.compilerManager = new BallerinaCompilerManager(this.languageClient());
        }
        
        return this.compilerManager;
    }

    public void init(LanguageClient client) {
        this.languageClient = languageClient;
        this.initialized = true;
    }
}
