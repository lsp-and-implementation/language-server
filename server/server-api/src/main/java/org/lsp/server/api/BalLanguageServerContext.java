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
package org.lsp.server.api;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.lsp.server.ballerina.compiler.workspace.CompilerManager;

import java.util.Optional;

/**
 * Ballerina language server context information.
 *
 * @since 1.0.0
 */
public interface BalLanguageServerContext {

    /**
     * Get the client capabilities.
     *
     * @return {@link ClientCapabilities}
     */
    Optional<ClientCapabilities> clientCapabilities();

    /**
     * Get the {@link ClientLogManager} instance.
     *
     * @return {@link ClientLogManager}
     */
    ClientLogManager logManager();

    /**
     * Get the diagnostic publisher.
     *
     * @return {@link DiagnosticsPublisher}
     */
    DiagnosticsPublisher diagnosticsPublisher();

    /**
     * Get the {@link LanguageClient} instance.
     * 
     * @return {@link LanguageClient}
     */
    LanguageClient languageClient();

    /**
     * Get the {@link CompilerManager} instance allocated to the server instance.
     * 
     * @return {@link CompilerManager}
     */
    CompilerManager compilerManager();
    
    
}
