package com.lspandimpl.server.core.completion.resolve;

public class AutoImportTextEditData {
    private final String uri;
    private final String importStatement;

    public AutoImportTextEditData(String uri, String importStatement) {
        this.uri = uri;
        this.importStatement = importStatement;
    }

    public String getUri() {
        return uri;
    }

    public String getImportStatement() {
        return importStatement;
    }
}
