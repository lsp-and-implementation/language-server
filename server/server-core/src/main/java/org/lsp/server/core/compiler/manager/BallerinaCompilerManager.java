package org.lsp.server.core.compiler.manager;

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
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.lsp.server.ballerina.compiler.workspace.CompilerManager;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BallerinaCompilerManager implements CompilerManager {

    private final Map<Path, Project> projectsMap = new ConcurrentHashMap<>();
    private final LanguageClient client;

    public BallerinaCompilerManager(LanguageClient client) {
        this.client = client;
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
    public Optional<Project> getProject(Path path) {
        return Optional.ofNullable(this.projectsMap.get(path));
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

    private Optional<DocumentId> getDocumentId(Path path) {
        Optional<Project> project = getProject(path);
        if (project.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(project.get().documentId(path));
    }

    private Optional<Document> getDocument(Path path) {
        Optional<Module> module = this.getModule(path);
        if (module.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(module.get().document(this.getDocumentId(path).orElseThrow()));
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
                            // TODO: use window.showDocument
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
}
