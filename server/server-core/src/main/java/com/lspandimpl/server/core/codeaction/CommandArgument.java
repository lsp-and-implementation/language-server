package com.lspandimpl.server.core.codeaction;

import com.google.gson.Gson;

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

    public <T> T getValue(Class<? extends T> clzz) {
        Gson gson = new Gson();
        return gson.fromJson(gson.toJsonTree(this.value), clzz);
    }

    public Object getValue() {
        return value;
    }
}
