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
package com.lspandimpl.server.core.compiler.manager;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.util.ProjectPaths;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextRange;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.ShowDocumentParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.services.LanguageClient;
import com.lspandimpl.server.api.context.LSContext;
import com.lspandimpl.server.ballerina.compiler.workspace.CompilerManager;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Ballerina Compiler Manager implementation.
 *
 * @since 1.0.0
 */
public class BallerinaCompilerManager extends CompilerManager {
    private static final LSContext.Key<CompilerManager> COMPILER_MANAGER_KEY = new LSContext.Key<>();
    private final Map<Path, Project> projectsMap = new ConcurrentHashMap<>();
    private final LanguageClient client;

    /**
     * Get the Compiler manager instance for the given server context.
     *
     * @param serverContext Language Server Context.
     * @return {@link CompilerManager} created instance
     */
    public static CompilerManager getInstance(LSContext serverContext) {
        CompilerManager compilerManager = serverContext.get(COMPILER_MANAGER_KEY);
        if (compilerManager == null) {
            compilerManager = new BallerinaCompilerManager(serverContext);
        }

        return compilerManager;
    }

    private BallerinaCompilerManager(LSContext serverContext) {
        serverContext.put(COMPILER_MANAGER_KEY, this);
        this.client = serverContext.getClient();
    }

    public Optional<Project> openDocument(Path path) {
        return this.buildProject(path);
    }

    public Optional<Project> updateDocument(Path path, String content) {
        Optional<Project> currentProject = this.getProject(path);
        if (currentProject.isEmpty()) {
            throw new RuntimeException("Trying to update a document which does not have an already opened Project");
        }

        Document modifiedDocument = this.getDocument(path).orElseThrow().modify().withContent(content).apply();
        Project newProject = modifiedDocument.module().project();
        this.projectsMap.put(currentProject.get().sourceRoot(), newProject);

        return Optional.ofNullable(newProject);
    }

    @Override
    public Optional<SemanticModel> getSemanticModel(Path path) {
        Optional<Module> module = this.getModule(path);
        if (module.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(module.get().getCompilation().getSemanticModel());
    }

    @Override
    public Optional<Project> getProject(Path path) {
        if (this.projectsMap.containsKey(path)) {
            return Optional.of(this.projectsMap.get(path));
        }
        if (ProjectPaths.isStandaloneBalFile(path)) {
            return Optional.ofNullable(this.projectsMap.get(path));
        }
        return Optional.ofNullable(this.projectsMap.get(ProjectPaths.packageRoot(path)));
    }

    @Override
    public Optional<Module> getModule(Path path) {
        Optional<Project> project = getProject(path);
        if (project.isEmpty()) {
            return Optional.empty();
        }

        ModuleId moduleId = getDocumentId(path).orElseThrow().moduleId();
        return Optional.ofNullable(project.get().currentPackage().module(moduleId));
    }

    @Override
    public Optional<SyntaxTree> getSyntaxTree(Path path) {
        DocumentId documentId = this.getDocumentId(path).orElseThrow();
        Module module = getModule(path).orElseThrow();

        return Optional.of(module.document(documentId).syntaxTree());
    }

    @Override
    public void invalidate(Path path) {
        this.projectsMap.remove(path);
    }

    @Override
    public Optional<Document> getDocument(Path path) {
        Optional<Module> module = this.getModule(path);
        if (module.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(module.get().document(this.getDocumentId(path).orElseThrow()));
    }

    @Override
    public Optional<Node> getNode(Path path, int line, int character) {
        Optional<Document> document = this.getDocument(path);
        if (document.isEmpty()) {
            return Optional.empty();
        }
        TextDocument textDocument = document.get().textDocument();
        int txtPos = textDocument.textPositionFrom(LinePosition.from(line, character));
        TextRange range = TextRange.from(txtPos, 0);
        NonTerminalNode nonTerminalNode = ((ModulePartNode) document.get().syntaxTree().rootNode()).findNode(range);

        return Optional.of(nonTerminalNode);
    }

    @Override
    public Optional<Path> getProjectRoot(Path path) {
        Path evalPath = getBallerinaFilePathInFolder(path);
        Optional<Project> project = this.getProject(evalPath);
        if (project.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ProjectPaths.packageRoot(evalPath));
    }

    @Override
    public void reloadProject(Path projectRoot) {
        // Todo: Implement
    }

    @Override
    public List<SemanticModel> getSemanticModels(Path projectRoot) {
        List<Module> modules = this.getModules(projectRoot);
        return modules.stream()
                .map(module -> module.getCompilation().getSemanticModel())
                .collect(Collectors.toList());
    }

    /**
     * Get the modules of a given project specified with the project root.
     *
     * @param path project root of thr project
     * @return {@link List} of modules in the project
     */
    public List<Module> getModules(Path path) {
        Optional<Project> project = getProject(path);
        if (project.isEmpty()) {
            return Collections.emptyList();
        }
        return StreamSupport.stream(project.get().currentPackage().modules().spliterator(), true)
                .collect(Collectors.toList());
    }

    private Optional<DocumentId> getDocumentId(Path path) {
        Optional<Project> project = getProject(path);
        if (project.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(project.get().documentId(path));
    }

    private Optional<Project> buildProject(Path path) {
        try {
            Project project;
            boolean isStandaloneFile = ProjectPaths.isStandaloneBalFile(path);
            Path packageRoot = isStandaloneFile ? path : ProjectPaths.packageRoot(path);
            BuildOptions options = new BuildOptionsBuilder().offline(true).build();

            if (this.projectsMap.containsKey(packageRoot)) {
                return Optional.of(this.projectsMap.get(packageRoot));
            }
            if (isStandaloneFile) {
                project = SingleFileProject.load(packageRoot, options);
            } else {
                project = BuildProject.load(packageRoot, options);
            }

            if (this.projectContainsTomlDiagnostics(project)) {
                ShowMessageRequestParams params = new ShowMessageRequestParams();
                MessageActionItem openBalToml = new MessageActionItem("Open Ballerina.toml");
                params.setMessage("Ballerina.toml contains Errors");
                params.setActions(Collections.singletonList(openBalToml));
                this.client.showMessageRequest(params)
                        .whenComplete((messageActionItem, throwable) -> {
                            ShowDocumentParams documentParams = new ShowDocumentParams();
                            documentParams.setExternal(false);
                            documentParams.setUri(project.sourceRoot().resolve("Ballerina.toml").toUri().toString());
                            documentParams.setTakeFocus(true);
                            client.showDocument(documentParams);
                        });
            }

            this.projectsMap.putIfAbsent(packageRoot, project);

            return Optional.of(project);
        } catch (ProjectException e) {
            return Optional.empty();
        }
    }

    private boolean projectContainsTomlDiagnostics(Project project) {
        DiagnosticResult diagnosticResult = project.currentPackage().getCompilation().diagnosticResult();
        return diagnosticResult.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic instanceof TomlDiagnostic
                        && diagnostic.location().lineRange().filePath().endsWith("Ballerina.toml"));
    }

    private Path getBallerinaFilePathInFolder(Path path) {
        File file = path.toFile();
        if (!file.isDirectory()) {
            return path;
        }
        if (path.resolve("Ballerina.toml").toFile().exists()) {
            return path.resolve("Ballerina.toml");
        }
        return Arrays.stream(Objects.requireNonNull(file.listFiles((dir, name) -> name.endsWith(".bal"))))
                .findAny().map(File::toPath).orElse(path);

    }
}
