package com.lspandimpl.server.ballerina.compiler.workspace;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Compiler Workspace Manager for Ballerina.
 * 
 * @since 1.0.0
 */
public abstract class CompilerManager {
    public abstract Optional<Project> getProject(Path path);
    
    public abstract Optional<Module> getModule(Path path);
    
    public abstract Optional<SyntaxTree> getSyntaxTree(Path path);

    public abstract void invalidate(Path path);

    public abstract Optional<Project> openDocument(Path path);

    public abstract Optional<Project> updateDocument(Path path, String content);
    
    public abstract Optional<SemanticModel> getSemanticModel(Path path);
    
    public abstract Optional<Document> getDocument(Path path);
    
    public abstract Optional<Node> getNode(Path path, int line, int character);
    
    public abstract Optional<Path> getProjectRoot(Path path);
    
    public abstract void reloadProject(Path projectRoot);
    
    public abstract List<SemanticModel> getSemanticModels(Path projectRoot);
    
    public abstract List<Module> getModules(Path path);
}
