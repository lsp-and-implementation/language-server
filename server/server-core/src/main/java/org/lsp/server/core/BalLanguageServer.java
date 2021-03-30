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

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.concurrent.CompletableFuture;

/**
 * Language server implementation for ballerina.
 *
 * @since 1.0.0
 */
public class BalLanguageServer implements LanguageServer, LanguageClientAware {
    private final TextDocumentService textDocumentService;
    private final WorkspaceService workspaceService;
    private final BallerinaLSContext serverContext;
    private LanguageClient client;
    private DynamicCapabilitySetter dynamicCapabilitySetter;
    private boolean shutdownInitiated = false;

    public BalLanguageServer() {
        this.serverContext = new BallerinaLSContext();
        this.textDocumentService = new BalTextDocumentService(this.serverContext);
        this.workspaceService = new BalWorkspaceService();
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        return CompletableFuture.supplyAsync(() -> {
            serverContext.setClientCapabilities(params.getCapabilities());
            ServerCapabilities sCapabilities = new ServerCapabilities();

            TextDocumentSyncOptions documentSyncOption = ServerInitUtils.getDocumentSyncOption();
            CompletionOptions completionOptions = ServerInitUtils.getCompletionOptions();

            sCapabilities.setTextDocumentSync(documentSyncOption);
            sCapabilities.setCompletionProvider(completionOptions);

            return new InitializeResult(sCapabilities);
        });
    }

    @Override
    public void initialized(InitializedParams params) {
        // Registering the onTypeFormatting capability
        this.dynamicCapabilitySetter.registerOnTypeFormatting(this.serverContext);
        // Other initializing tasks can be handled here
        MessageParams messageParams = new MessageParams();
        messageParams.setMessage("Server Initiated!");
        messageParams.setType(MessageType.Info);
        this.client.showMessage(messageParams);
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        this.shutdownInitiated = true;

        return CompletableFuture.supplyAsync(Object::new);
    }

    @Override
    public void exit() {
        // the flag is true when the client sends the shutdown request
        // Gracefully exit server process
        if (this.shutdownInitiated) {
            System.exit(0);
        }
        System.exit(1);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return this.textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return this.workspaceService;
    }

    @Override
    public void connect(LanguageClient languageClient) {
        this.client = languageClient;
        this.serverContext.setClient(this.client);
    }
}
