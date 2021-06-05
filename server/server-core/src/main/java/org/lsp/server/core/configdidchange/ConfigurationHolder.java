package org.lsp.server.core.configdidchange;

import com.google.gson.JsonElement;

public class ConfigurationHolder {
    private static ConfigurationHolder instance = null;
    public static final String CONFIG_SECTION = "ballerina";

    private ConfigurationHolder() {
    }
    
    public static ConfigurationHolder getInstance() {
        if (instance == null) {
            instance= new ConfigurationHolder();
        }
        
        return instance;
    }
    
    public void update(JsonElement element) {}
    
    public boolean isDocumentationCodeActionEnabled() {
        return false;
    }
}
