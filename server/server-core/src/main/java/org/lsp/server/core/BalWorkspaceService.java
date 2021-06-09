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
import io.ballerina.projects.Project;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.lsp.server.api.context.BalWorkspaceContext;
import org.lsp.server.api.context.LSContext;
import org.lsp.server.core.codeaction.Command;
import org.lsp.server.core.configdidchange.ConfigurationHolder;
import org.lsp.server.core.contexts.ContextBuilder;
import org.lsp.server.core.utils.CommonUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        params.getEvent().getRemoved();
    }

    @Override
    public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
        return CompletableFuture.supplyAsync(() -> {
            String command = params.getCommand();
            
            if (command.equals(Command.CREATE_VAR.getName())) {
                
            }
            return null;
        });
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
            Optional<Path> project =
                    context.compilerManager().getProjectRoot(path);
            project.ifPresent(projects::add);
        }

        return projects;
    }
    
    
}
