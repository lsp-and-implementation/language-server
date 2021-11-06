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
package com.lspandimpl.server.core;

import io.ballerina.projects.util.ProjectConstants;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.DidChangeWatchedFilesRegistrationOptions;
import org.eclipse.lsp4j.FileSystemWatcher;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.InitializedParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncOptions;
import org.eclipse.lsp4j.WatchKind;
import org.eclipse.lsp4j.WorkspaceServerCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import com.lspandimpl.server.core.extensions.services.parser.BallerinaParserService;
import com.lspandimpl.server.core.extensions.services.parser.impl.BallerinaParserServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Language server implementation for ballerina.
 *
 * @since 1.0.0
 */
public class BalLanguageServer implements BalExtendedLanguageServer, LanguageClientAware {
    private final TextDocumentService textDocumentService;
    private final WorkspaceService workspaceService;
    private final BallerinaLSContext serverContext;
    private LanguageClient client;
    private DynamicCapabilitySetter dynamicCapabilitySetter;
    private BallerinaParserService parserService;
    private boolean shutdownInitiated = false;

    public BalLanguageServer() {
        this.serverContext = new BallerinaLSContext();
        this.textDocumentService = new BalTextDocumentService(this.serverContext);
        this.workspaceService = new BalWorkspaceService(this.serverContext);
        this.dynamicCapabilitySetter = DynamicCapabilitySetter.getInstance(this.serverContext);
        this.parserService = new BallerinaParserServiceImpl(this.serverContext);
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        return CompletableFuture.supplyAsync(() -> {
            serverContext.setClientCapabilities(params.getCapabilities());
            ServerCapabilities sCapabilities = new ServerCapabilities();
            WorkspaceServerCapabilities wsCapabilities = new WorkspaceServerCapabilities();
            TextDocumentSyncOptions documentSyncOption = ServerInitUtils.getDocumentSyncOption();
            CompletionOptions completionOptions = ServerInitUtils.getCompletionOptions();

            sCapabilities.setTextDocumentSync(documentSyncOption);
            sCapabilities.setCompletionProvider(completionOptions);
            sCapabilities.setRenameProvider(ServerInitUtils.getRenameOptions());
            sCapabilities.setDocumentFormattingProvider(true);
            sCapabilities.setDocumentRangeFormattingProvider(true);
            // Comment the following before enabling dynamic registration
            // sCapabilities.setDocumentOnTypeFormattingProvider(ServerInitUtils.getOnTypeFormatOptions());
            sCapabilities.setDocumentSymbolProvider(ServerInitUtils.getDocumentSymbolOptions());
            sCapabilities.setDocumentHighlightProvider(true);
            sCapabilities.setDocumentLinkProvider(ServerInitUtils.getDocumentLinkOptions());
            sCapabilities.setSelectionRangeProvider(true);
            sCapabilities.setLinkedEditingRangeProvider(true);
            sCapabilities.setSemanticTokensProvider(ServerInitUtils.getSemanticTokenOptions());
            sCapabilities.setCodeActionProvider(ServerInitUtils.getCodeActionOptions());
            sCapabilities.setSignatureHelpProvider(ServerInitUtils.getSignatureHelpOptions());
            sCapabilities.setColorProvider(true);
            sCapabilities.setCodeLensProvider(ServerInitUtils.getCodeLensOptions());
            sCapabilities.setFoldingRangeProvider(true);
            sCapabilities.setCallHierarchyProvider(true);
            sCapabilities.setHoverProvider(Either.forRight(ServerInitUtils.getHoverOptions()));
            sCapabilities.setReferencesProvider(Either.forRight(ServerInitUtils.getReferencesOptions()));
            sCapabilities.setExecuteCommandProvider(ServerInitUtils.getExecuteCommandOptions());
            sCapabilities.setDefinitionProvider(ServerInitUtils.getDefinitionOptions());
            sCapabilities.setTypeDefinitionProvider(ServerInitUtils.getTypeDefinitionOptions());
            sCapabilities.setImplementationProvider(ServerInitUtils.getImplementationOptions());
            sCapabilities.setDeclarationProvider(ServerInitUtils.getDeclarationOptions());
            sCapabilities.setWorkspaceSymbolProvider(ServerInitUtils.getWorkspaceSymbolOptions());

            // Set the workspace capabilities
            wsCapabilities.setWorkspaceFolders(ServerInitUtils.getWorkspaceFolderOptions());
            sCapabilities.setWorkspace(wsCapabilities);

            return new InitializeResult(sCapabilities);
        });
    }

    @Override
    public void initialized(InitializedParams params) {
        // Other initializing tasks can be handled here
        MessageParams messageParams = new MessageParams();
        messageParams.setMessage("Server Initiated!");
        messageParams.setType(MessageType.Info);
        // Registering the onTypeFormatting capability.
        // If enable the following, comment out the ontype formatting in the initialize method
        this.dynamicCapabilitySetter.registerOnTypeFormatting(this.serverContext);
        this.dynamicCapabilitySetter.registerTextDocumentSyncOptions(serverContext);
        this.client.showMessage(messageParams);

        // Register file watchers
        List<FileSystemWatcher> watchers = new ArrayList<>();
        watchers.add(new FileSystemWatcher("/**/"
                + ProjectConstants.BALLERINA_TOML,
                WatchKind.Create + WatchKind.Delete + WatchKind.Change));
        watchers.add(new FileSystemWatcher("/**/"
                + ProjectConstants.CLOUD_TOML,
                WatchKind.Create + WatchKind.Delete));
        DidChangeWatchedFilesRegistrationOptions opts =
                new DidChangeWatchedFilesRegistrationOptions(watchers);
        Registration registration =
                new Registration(UUID.randomUUID().toString(),
                        "workspace/didChangeWatchedFiles", opts);
        this.client.registerCapability(
                new RegistrationParams(Collections.singletonList(registration)));
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

    @Override
    public BallerinaParserService getBallerinaParserService() {
        return null;
    }
}
