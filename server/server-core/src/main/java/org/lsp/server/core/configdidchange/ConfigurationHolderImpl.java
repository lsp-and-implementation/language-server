package org.lsp.server.core.configdidchange;

import com.google.gson.JsonElement;
import org.lsp.server.api.ConfigurationHolder;
import org.lsp.server.api.context.LSContext;

public class ConfigurationHolderImpl implements ConfigurationHolder {
    private static final LSContext.Key<ConfigurationHolder> CONFIGURATION_HOLDER_KEY = new LSContext.Key<>();
    private JsonElement config = null;

    public static ConfigurationHolder getInstance(LSContext serverContext) {
        ConfigurationHolder configurationHolder = serverContext.get(CONFIGURATION_HOLDER_KEY);
        if (configurationHolder == null) {
            configurationHolder = new ConfigurationHolderImpl(serverContext);
        }

        return configurationHolder;
    }

    private ConfigurationHolderImpl(LSContext serverContext) {
        serverContext.put(CONFIGURATION_HOLDER_KEY, this);
    }

    public synchronized void update(JsonElement newConfig) {
        this.config = newConfig;
    }

    public boolean isDocumentationCodeActionEnabled() {
        if (this.config == null) {
            return false;
        }

        return false;
    }
}
