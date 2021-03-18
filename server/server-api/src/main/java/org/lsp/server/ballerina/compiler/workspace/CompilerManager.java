package org.lsp.server.ballerina.compiler.workspace;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Compiler Workspace Manager for Ballerina.
 * 
 * @since 1.0.0
 */
public interface CompilerManager {
    Optional<Project> getProject(Path path);
    
    Optional<Module> getModule(Path path);
    
    Optional<SyntaxTree> getSyntaxTree(Path path);

    void invalidate(Path path);

    Optional<Project> openDocument(Path path);

    Optional<Project> updateDocument(Path path, String content);
}
