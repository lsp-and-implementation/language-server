package com.lspandimpl.server.api.context;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;

import java.nio.file.Path;
import java.util.Optional;

public interface BalTextDocumentContext extends BaseOperationContext {
    /**
     * Get the current document path.
     *
     * @return {@link Path}
     */
    Path getPath();
    
    Optional<Document> currentDocument();
    
    Optional<SyntaxTree> currentSyntaxTree();
}
