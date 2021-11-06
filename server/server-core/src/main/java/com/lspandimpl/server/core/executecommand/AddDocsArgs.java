package com.lspandimpl.server.core.executecommand;

public class AddDocsArgs {
    private final String name;
    private final String uri;

    public AddDocsArgs(String name, String uri) {
        this.name = name;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }
}
