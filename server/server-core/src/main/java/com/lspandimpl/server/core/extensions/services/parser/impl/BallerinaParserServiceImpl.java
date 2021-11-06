package com.lspandimpl.server.core.extensions.services.parser.impl;

import com.lspandimpl.server.api.context.LSContext;
import com.lspandimpl.server.core.extensions.services.parser.NodeResponse;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import com.lspandimpl.server.core.extensions.services.parser.BallerinaParserService;

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
