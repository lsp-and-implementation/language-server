package org.lsp.server.core.codeaction;

public enum Command {
    CREATE_VAR("CREATE_VAR", "Create Variable"),
    ADD_DOC("ADD_DOC", "Add Documentation");

    private String command;
    private String title;
    
    Command(String command, String title) {
        this.command = command;
        this.title = title;
    }

    public String getName() {
        return command;
    }

    public String getTitle() {
        return title;
    }
}
