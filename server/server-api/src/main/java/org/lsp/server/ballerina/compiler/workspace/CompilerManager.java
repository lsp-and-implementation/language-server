package org.lsp.server.ballerina.compiler.workspace;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import org.lsp.server.api.LSContext;

import java.nio.file.Path;
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
}
