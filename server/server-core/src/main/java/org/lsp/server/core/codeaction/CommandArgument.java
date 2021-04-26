package org.lsp.server.core.codeaction;

public class CommandArgument {
    private final String key;
    private final Object value;

    public CommandArgument(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
}
