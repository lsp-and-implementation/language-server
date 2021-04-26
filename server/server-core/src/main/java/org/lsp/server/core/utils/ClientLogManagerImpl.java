package org.lsp.server.core.utils;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;
import org.lsp.server.api.ClientLogManager;
import org.lsp.server.api.context.LSContext;

/**
 * Simplified Logging manager which logs messages to the language client via client.logMessage.
 * 
 * @since 1.0.0
 */
public class ClientLogManagerImpl implements ClientLogManager {
    private static final LSContext.Key<ClientLogManager> CLIENT_LOG_MANAGER_KEY = new LSContext.Key<>();

    private final LanguageClient client;

    private ClientLogManagerImpl(LanguageClient client) {
        this.client = client;
    }

    public static ClientLogManager getInstance(LSContext serverContext) {
        ClientLogManager clientLogManager = serverContext.get(CLIENT_LOG_MANAGER_KEY);
        if (clientLogManager == null) {
            clientLogManager = new ClientLogManagerImpl(serverContext.getClient());
        }

        return clientLogManager;
    }
    
    @Override
    public void publishInfo(String message) {
        this.client.logMessage(this.getMessageParams(message, MessageType.Info));
    }

    @Override
    public void publishLog(String message) {
        this.client.logMessage(this.getMessageParams(message, MessageType.Log));
    }

    @Override
    public void publishError(String message) {
        this.client.logMessage(this.getMessageParams(message, MessageType.Error));
    }

    @Override
    public void publishWarning(String message) {
        this.client.logMessage(this.getMessageParams(message, MessageType.Warning));
    }
    
    public void showErrorMessage(String message) {
        this.client.showMessage(this.getMessageParams(message, MessageType.Error));
    }
    
    private MessageParams getMessageParams(String message, MessageType type) {
        MessageParams messageParams = new MessageParams();
        messageParams.setMessage(message);
        messageParams.setType(type);
        
        return messageParams;
    }
}
