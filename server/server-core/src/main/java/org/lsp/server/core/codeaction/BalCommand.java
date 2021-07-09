package org.lsp.server.core.codeaction;

public enum BalCommand {
    CREATE_VAR("CREATE_VAR", "Create Variable"),
    ADD_DOC("ADD_DOC", "Add Documentation");

    private final String command;
    private final String title;
    
    BalCommand(String command, String title) {
        this.command = command;
        this.title = title;
    }

    public String getCommand() {
        return command;
    }

    public String getTitle() {
        return title;
    }
}
