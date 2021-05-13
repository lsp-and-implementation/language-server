package org.lsp.server.core.contexts;

import io.ballerina.projects.Document;
import org.lsp.server.api.context.BalTextDocumentContext;
import org.lsp.server.api.context.LSContext;
import org.lsp.server.core.compiler.manager.BallerinaCompilerManager;
import org.lsp.server.core.utils.CommonUtils;

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
}
