package org.lsp.server.core.extensions.services.parser.impl;

import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.lsp.server.api.context.LSContext;
import org.lsp.server.core.extensions.services.parser.BallerinaParserService;
import org.lsp.server.core.extensions.services.parser.NodeResponse;

import java.util.concurrent.CompletableFuture;

public class BallerinaParserServiceImpl implements BallerinaParserService {
    private LSContext serverContext;

    public BallerinaParserServiceImpl(LSContext serverContext) {
        this.serverContext = serverContext;
    }

    @Override
    public CompletableFuture<NodeResponse> node(TextDocumentPositionParams params) {
        return CompletableFuture.supplyAsync(NodeResponse::new);
    }
}
