package com.lspandimpl.server.core.extensions.services.parser;

import java.util.List;

/**
 * Node response for the textDocument/parser/node request.
 */
public class NodeResponse {
    private String nodeKind;
    private List<String> diagnostics;

    public String getNodeKind() {
        return nodeKind;
    }

    public void setNodeKind(String nodeKind) {
        this.nodeKind = nodeKind;
    }

    public List<String> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<String> diagnostics) {
        this.diagnostics = diagnostics;
    }
}
