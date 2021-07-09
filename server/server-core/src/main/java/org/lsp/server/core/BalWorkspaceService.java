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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.ballerina.compiler.api.SemanticModel;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.WorkDoneProgressReport;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.lsp.server.api.context.BalWorkspaceContext;
import org.lsp.server.api.context.LSContext;
import org.lsp.server.core.codeaction.BalCommand;
import org.lsp.server.core.codeaction.CodeActionProvider;
import org.lsp.server.core.codeaction.CommandArgument;
import org.lsp.server.core.configdidchange.ConfigurationHolder;
import org.lsp.server.core.contexts.ContextBuilder;
import org.lsp.server.core.executecommand.CreateVariableArgs;
import org.lsp.server.core.utils.CommonUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class BalWorkspaceService implements WorkspaceService {
    private static final String TOML_CONFIG = "Ballerina.toml";
    private final LSContext lsServerContext;

    public BalWorkspaceService(LSContext lsServerContext) {
        this.lsServerContext = lsServerContext;
    }

    @Override
    public void didChangeConfiguration(
            DidChangeConfigurationParams params) {
        JsonObject settings = (JsonObject) params.getSettings();
        JsonElement configSection =
                settings.get(ConfigurationHolder.CONFIG_SECTION);
        if (configSection != null) {
            ConfigurationHolder.getInstance().update(configSection);
        }
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        BalWorkspaceContext context =
                ContextBuilder.getWorkspaceContext(this.lsServerContext);
        Optional<FileEvent> tomlEvent = params.getChanges().stream()
                .filter(fileEvent -> fileEvent.getUri().endsWith(TOML_CONFIG)
                        && fileEvent.getType() != FileChangeType.Changed)
                .findAny();

        if (tomlEvent.isPresent()) {
            Path path = CommonUtils.uriToPath(tomlEvent.get().getUri());
            Optional<Path> projectRoot =
                    context.compilerManager().getProjectRoot(path);
            if (projectRoot.isEmpty()) {
                return;
            }
            // Reload the project
            context.compilerManager().reloadProject(projectRoot.get());
            // Send codelens refresh request to client
            LanguageClient client = this.lsServerContext.getClient();
            client.refreshCodeLenses();
        }
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>>
    symbol(WorkspaceSymbolParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalWorkspaceContext context =
                    ContextBuilder.getWorkspaceContext(this.lsServerContext);
            List<SymbolInformation> wsSymbols = new ArrayList<>();
            try {
                List<Path> projectRoots = this.getProjectRoots(context);
                for (Path projectRoot : projectRoots) {
                    List<SemanticModel> semanticModels =
                            context.compilerManager()
                                    .getSemanticModels(projectRoot);
                    for (SemanticModel semanticModel : semanticModels) {
                        List<SymbolInformation> symbols =
                                semanticModel.moduleSymbols().stream()
                                        .map(CommonUtils::getSymbolInformation)
                                        .collect(Collectors.toList());
                        wsSymbols.addAll(symbols);
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                // Ignore
            }
            return wsSymbols;
        });
    }

    @Override
    public void didChangeWorkspaceFolders(DidChangeWorkspaceFoldersParams params) {
        BalWorkspaceContext context = ContextBuilder.getWorkspaceContext(this.lsServerContext);
        List<WorkspaceFolder> added = params.getEvent().getAdded();
        List<WorkspaceFolder> removed = params.getEvent().getRemoved();

        Runnable task = () -> {
            try {
                this.reIndexWorkspace(context, this.lsServerContext.getClient());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }

    @Override
    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        return CompletableFuture.supplyAsync(() -> {
            String command = params.getCommand();
            BalWorkspaceContext context =
                    ContextBuilder.getWorkspaceContext(this.lsServerContext);

            if (command.equals(BalCommand.CREATE_VAR.getCommand())) {
                return applyCreateVarWorkspaceEdit(context, params);
            }
            
            return null;
        });
    }
    
    private ApplyWorkspaceEditResponse
    applyCreateVarWorkspaceEdit(BalWorkspaceContext context, ExecuteCommandParams params) {
        CommandArgument commandArg = (CommandArgument) params.getArguments().get(0);
        if (!commandArg.getKey().equals("params")) {
            return null;
        }
        CreateVariableArgs createVarArgs = (CreateVariableArgs) commandArg.getValue();
        WorkspaceEdit workspaceEdit = new WorkspaceEdit();
        TextDocumentEdit documentEdit = new TextDocumentEdit();
        VersionedTextDocumentIdentifier identifier =
                new VersionedTextDocumentIdentifier();
        identifier.setUri(createVarArgs.getUri());
        TextEdit textEdit = new TextEdit(createVarArgs.getRange(), createVarArgs.getNewText());
        
        documentEdit.setEdits(Collections.singletonList(textEdit));
        documentEdit.setTextDocument(identifier);
        Either<TextDocumentEdit, ResourceOperation> documentChanges = Either.forLeft(documentEdit);
        workspaceEdit.setDocumentChanges(Collections.singletonList(documentChanges));

        ApplyWorkspaceEditParams applyEditParams = new ApplyWorkspaceEditParams();
        applyEditParams.setEdit(workspaceEdit);
        CompletableFuture<ApplyWorkspaceEditResponse> response = context.getClient().applyEdit(applyEditParams);

        try {
            return response.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Path> getProjectRoots(BalWorkspaceContext context)
            throws ExecutionException, InterruptedException {
        LanguageClient client = this.lsServerContext.getClient();
        // Invoke the workspace/workspaceFolders
        CompletableFuture<List<WorkspaceFolder>> result
                = client.workspaceFolders();
        List<Path> projects = new ArrayList<>();
        for (WorkspaceFolder workspaceFolder : result.get()) {
            Path path = CommonUtils.uriToPath(workspaceFolder.getUri());
            path = path.resolve("modules").resolve("mod1").resolve("mod1.bal");
            Optional<Path> project =
                    context.compilerManager().getProjectRoot(path);
            project.ifPresent(projects::add);
        }

        return projects;
    }

    private void reIndexWorkspace(BalWorkspaceContext context,
                                  LanguageClient client)
            throws ExecutionException, InterruptedException {
        List<Path> projectRoots = getProjectRoots(context);
        WorkDoneProgressCreateParams progressCreate =
                new WorkDoneProgressCreateParams();
        UUID uuid = UUID.randomUUID();
        progressCreate.setToken(uuid.toString());
        // Send the begin progress notification
        client.createProgress(progressCreate);

        // Notify the begin progress
        WorkDoneProgressBegin begin = new WorkDoneProgressBegin();
        begin.setTitle("Indexing");
        ProgressParams beginParams = new ProgressParams();
        beginParams.setValue(Either.forLeft(begin));
        beginParams.setToken(Either.forLeft(uuid.toString()));
        client.notifyProgress(beginParams);

        for (int i = 0; i < projectRoots.size(); i++) {
            WorkDoneProgressReport reportProgress =
                    new WorkDoneProgressReport();
            reportProgress.setCancellable(false);
            reportProgress
                    .setPercentage(((i + 1) / projectRoots.size()) * 100);
            reportProgress.setMessage((i + 1)
                    + " out of " + projectRoots.size()
                    + " projects being indexing");
            ProgressParams params = new ProgressParams();
            params.setToken(uuid.toString());
            params.setValue(Either.forLeft(reportProgress));
            client.notifyProgress(params);
            // Here goes the indexing logic.
            // For testing purposes we add a thread sleep
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Notify Progress End
        WorkDoneProgressEnd endProgress = new WorkDoneProgressEnd();
        endProgress.setMessage("Successfully indexed "
                + projectRoots.size() + " projects");
        ProgressParams endParams = new ProgressParams();
        endParams.setToken(uuid.toString());
        endParams.setValue(Either.forLeft(endProgress));
        client.notifyProgress(endParams);
        
    }
}
