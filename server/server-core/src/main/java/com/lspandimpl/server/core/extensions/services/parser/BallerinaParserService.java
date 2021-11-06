package com.lspandimpl.server.core.extensions.services.parser;

import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

import java.util.concurrent.CompletableFuture;

@JsonSegment("textDocument/parser")
public interface BallerinaParserService {
    /**
     * Operation name would be picked as node and the client will
     * call the operation textDocument/parser/node
     * 
     * @param params input parameters
     * @return node response
     */
    @JsonRequest
    CompletableFuture<NodeResponse>node(TextDocumentPositionParams params);
}
