package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.LSContext;
import com.lspandimpl.server.core.compiler.manager.BallerinaCompilerManager;
import com.lspandimpl.server.core.utils.CommonUtils;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Document;
import com.lspandimpl.server.api.context.BalTextDocumentContext;

import java.nio.file.Path;
import java.util.Optional;

public class BalTextDocumentContextImpl extends BaseOperationContextImpl implements BalTextDocumentContext {
    private final LSContext serverContext;
    private final String uri;

    public BalTextDocumentContextImpl(LSContext serverContext, String uri) {
        super(serverContext);
        this.serverContext = serverContext;
        this.uri = uri;
    }

    @Override
    public Path getPath() {
        return CommonUtils.uriToPath(uri);
    }

    @Override
    public Optional<Document> currentDocument() {
        Path path = CommonUtils.uriToPath(this.uri);

        return BallerinaCompilerManager.getInstance(this.serverContext).getDocument(path);
    }

    @Override
    public Optional<SyntaxTree> currentSyntaxTree() {
        Path path = CommonUtils.uriToPath(this.uri);
        
        return BallerinaCompilerManager.getInstance(this.serverContext).getSyntaxTree(path);
    }
}
