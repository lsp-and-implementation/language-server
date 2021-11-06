package com.lspandimpl.server.api;

import com.google.gson.JsonElement;

public interface ConfigurationHolder {
    String CONFIG_SECTION = "ballerina";
    String ADD_DOCS_CODE_ACTION = "ballerina.codeAction.documentation";

    void update(JsonElement element);

    boolean isDocumentationCodeActionEnabled();
}
