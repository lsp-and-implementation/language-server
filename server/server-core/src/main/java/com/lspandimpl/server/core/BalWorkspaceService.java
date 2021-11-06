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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lspandimpl.server.core.utils.CommonUtils;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.Range;
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
import com.lspandimpl.server.api.context.BalWorkspaceContext;
import com.lspandimpl.server.api.context.LSContext;
import com.lspandimpl.server.core.codeaction.BalCommand;
import com.lspandimpl.server.core.codeaction.CommandArgument;
import com.lspandimpl.server.core.configdidchange.ConfigurationHolderImpl;
import com.lspandimpl.server.core.contexts.ContextBuilder;
import com.lspandimpl.server.core.executecommand.AddDocsArgs;
import com.lspandimpl.server.core.executecommand.CreateVariableArgs;
import com.lspandimpl.server.core.wsfolderchange.WSFolderChangeHandler;

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
    private static final String BALLERINA_TOML = "Ballerina.toml";
    private static final String CLOUD_TOML = "Cloud.toml";
    private final LSContext lsServerContext;

    public BalWorkspaceService(LSContext lsServerContext) {
        this.lsServerContext = lsServerContext;
    }

    @Override
    public void didChangeConfiguration(
            DidChangeConfigurationParams params) {
        BalWorkspaceContext context =
                ContextBuilder.getWorkspaceContext(this.lsServerContext);
        JsonObject settings = (JsonObject) params.getSettings();
        JsonElement configSection =
                settings.get(ConfigurationHolderImpl.CONFIG_SECTION);
        if (configSection != null) {
            context.clientConfigHolder().update(configSection);
        }
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        BalWorkspaceContext context =
                ContextBuilder.getWorkspaceContext(this.lsServerContext);
        Optional<FileEvent> ballerinaTomlEvent = params.getChanges().stream()
                .filter(fileEvent -> fileEvent.getUri().endsWith(BALLERINA_TOML)
                        && fileEvent.getType() == FileChangeType.Changed)
                .findAny();
        Optional<FileEvent> cloudTomlEvent = params.getChanges().stream()
                .filter(fileEvent -> fileEvent.getUri().endsWith(CLOUD_TOML)
                        && fileEvent.getType() == FileChangeType.Changed)
                .findAny();

        if (ballerinaTomlEvent.isPresent()) {
            Path path = CommonUtils.uriToPath(ballerinaTomlEvent.get().getUri());
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
                                        .filter(s -> s.getName().isPresent())
                                        .map(symbol -> CommonUtils
                                                .getSymbolInformation(symbol, context, projectRoot))
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
        // Note: Enable the following to support the following scenario, which is a hypothetical usage example
        /*
        In this example scenario we assume that the user opens a module at a time in the workspace,
        instead of opening the root project folder and once a new module is added through the file system the user
        adds the module to the workspace manually
         */
        WSFolderChangeHandler.updateProjects(context, params);

        Runnable task = () -> {
            try {
                this.reIndexWorkspace(context, this.lsServerContext.getClient());
            } catch (ExecutionException | InterruptedException e) {
                // ignore
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
            if (command.equals(BalCommand.ADD_DOC.getCommand())) {
                return applyAddDocumentationWorkspaceEdit(context, params);
            }

            // TODO: IMPLEMENT THE MOVE FUNCTION CODE ACTION WHICH CREATES A FILE

            return null;
        });
    }

    private ApplyWorkspaceEditResponse
    applyCreateVarWorkspaceEdit(BalWorkspaceContext context,
                                ExecuteCommandParams params) {
        JsonObject arg = (JsonObject) params.getArguments().get(0);
        CommandArgument commandArg = new Gson().fromJson(arg, CommandArgument.class);
        if (!commandArg.getKey().equals("params")) {
            return null;
        }
        CreateVariableArgs createVarArgs = commandArg.getValue(CreateVariableArgs.class);
        WorkspaceEdit workspaceEdit = new WorkspaceEdit();
        TextDocumentEdit documentEdit = new TextDocumentEdit();
        VersionedTextDocumentIdentifier identifier =
                new VersionedTextDocumentIdentifier();
        identifier.setUri(createVarArgs.getUri());
        TextEdit textEdit = new TextEdit(createVarArgs.getRange(),
                createVarArgs.getNewText());

        documentEdit.setEdits(Collections.singletonList(textEdit));
        documentEdit.setTextDocument(identifier);
        Either<TextDocumentEdit, ResourceOperation> documentChanges =
                Either.forLeft(documentEdit);
        workspaceEdit.setDocumentChanges(Collections.singletonList(documentChanges));

        ApplyWorkspaceEditParams applyEditParams = new ApplyWorkspaceEditParams();
        applyEditParams.setEdit(workspaceEdit);
        CompletableFuture<ApplyWorkspaceEditResponse> response =
                context.getClient().applyEdit(applyEditParams);

        try {
            return response.get();
        } catch (InterruptedException | ExecutionException e) {
            // TODO: handle gracefully
        }
        return null;
    }

    private ApplyWorkspaceEditResponse applyAddDocumentationWorkspaceEdit(BalWorkspaceContext context,
                                                                          ExecuteCommandParams params) {
        JsonObject arg = (JsonObject) params.getArguments().get(0);
        CommandArgument commandArg = new Gson().fromJson(arg, CommandArgument.class);
        if (!commandArg.getKey().equals("params")) {
            return null;
        }
        AddDocsArgs addDocsArgs = commandArg.getValue(AddDocsArgs.class);

        // get the function node and calculate docs
        String uri = addDocsArgs.getUri();
        Optional<SyntaxTree> syntaxTree = context.compilerManager().getSyntaxTree(CommonUtils.uriToPath(uri));
        WorkspaceEdit wsEdit = getDocumentationForFunction(syntaxTree.get(), addDocsArgs.getName(), uri);
        ApplyWorkspaceEditParams applyEditParams = new ApplyWorkspaceEditParams();
        applyEditParams.setEdit(wsEdit);
        CompletableFuture<ApplyWorkspaceEditResponse> response =
                context.getClient().applyEdit(applyEditParams);

        try {
            return response.get();
        } catch (InterruptedException | ExecutionException e) {
            // TODO: handle gracefully
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
            Path path = CommonUtils.uriToPath(workspaceFolder.getUri()).resolve("Ballerina.toml");
            // Ballerina project contains a Ballerina.toml 
            if (!path.toFile().exists()) {
                continue;
            }
            Optional<Path> project =
                    context.compilerManager().getProjectRoot(path);
            project.ifPresent(projects::add);
        }

        return projects;
    }

    private List<Path> getAllProjectRoots(BalWorkspaceContext context)
            throws ExecutionException, InterruptedException {
        LanguageClient client = this.lsServerContext.getClient();
        // Invoke the workspace/workspaceFolders
        CompletableFuture<List<WorkspaceFolder>> result
                = client.workspaceFolders();
        List<Path> projects = new ArrayList<>();
        for (WorkspaceFolder workspaceFolder : result.get()) {
            Path path = CommonUtils.uriToPath(workspaceFolder.getUri()).resolve("Ballerina.toml");
            // Ballerina project contains a Ballerina.toml 
            if (!path.toFile().exists()) {
                continue;
            }
            projects.add(path);
        }

        return projects;
    }

    private void reIndexWorkspace(BalWorkspaceContext context,
                                  LanguageClient client)
            throws ExecutionException, InterruptedException {
        List<Path> projectRoots = getAllProjectRoots(context);
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
                // ignore
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

    private WorkspaceEdit getDocumentationForFunction(SyntaxTree tree, String name, String uri) {
        Optional<ModuleMemberDeclarationNode> function = ((ModulePartNode) tree.rootNode()).members().stream()
                .filter(member -> member.kind() == SyntaxKind.FUNCTION_DEFINITION
                        && ((FunctionDefinitionNode) member).functionName().text().equals(name))
                .findFirst();

        if (function.isEmpty()) {
            return null;
        }
        FunctionDefinitionNode functionNode = (FunctionDefinitionNode) function.get();
        StringBuilder docs = new StringBuilder("# Description");
        for (ParameterNode parameter : functionNode.functionSignature().parameters()) {
            String paramName;
            if (parameter.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
                paramName = ((DefaultableParameterNode) parameter).paramName().get().text();
            } else if (parameter.kind() == SyntaxKind.REQUIRED_PARAM) {
                paramName = ((RequiredParameterNode) parameter).paramName().get().text();
            } else {
                paramName = ((RestParameterNode) parameter).paramName().get().text();
            }

            docs.append(System.lineSeparator())
                    .append("# + ")
                    .append(paramName)
                    .append(" - ")
                    .append(paramName)
                    .append(" description");
        }
        Optional<ReturnTypeDescriptorNode> returnType = functionNode.functionSignature().returnTypeDesc();
        if (returnType.isPresent()) {
            docs.append(System.lineSeparator())
                    .append("# + return - Return type description");
        }
        docs.append(System.lineSeparator());

        WorkspaceEdit workspaceEdit = new WorkspaceEdit();
        TextDocumentEdit documentEdit = new TextDocumentEdit();
        VersionedTextDocumentIdentifier identifier =
                new VersionedTextDocumentIdentifier();
        identifier.setUri(uri);
        LinePosition startLine = functionNode.lineRange().startLine();
        Range range = new Range();
        range.setStart(CommonUtils.toPosition(startLine));
        range.setEnd(CommonUtils.toPosition(startLine));

        TextEdit textEdit = new TextEdit(range, docs.toString());

        documentEdit.setEdits(Collections.singletonList(textEdit));
        documentEdit.setTextDocument(identifier);
        Either<TextDocumentEdit, ResourceOperation> documentChanges =
                Either.forLeft(documentEdit);
        workspaceEdit.setDocumentChanges(Collections.singletonList(documentChanges));

        return workspaceEdit;
    }
}
