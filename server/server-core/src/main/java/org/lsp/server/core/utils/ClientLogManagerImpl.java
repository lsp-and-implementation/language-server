package org.lsp.server.core.utils;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;
import org.lsp.server.api.ClientLogManager;

/**
 * Simplified Logging manager which logs messages to the language client via client.logMessage.
 * 
 * @since 1.0.0
 */
public class ClientLogManagerImpl implements ClientLogManager {
    private final LanguageClient client;

    public ClientLogManagerImpl(LanguageClient client) {
        this.client = client;
    }
    
    @Override
    public void publishInfo(String message) {
        MessageParams params = this.getMessageParams(message);
        params.setType(MessageType.Info);
        
        this.client.logMessage(params);
    }

    @Override
    public void publishLog(String message) {
        MessageParams params = this.getMessageParams(message);
        params.setType(MessageType.Log);
        
        this.client.logMessage(params);
    }

    @Override
    public void publishError(String message) {
        MessageParams params = this.getMessageParams(message);
        params.setType(MessageType.Error);
        
        this.client.logMessage(params);
    }

    @Override
    public void publishWarning(String message) {
        MessageParams params = this.getMessageParams(message);
        params.setType(MessageType.Warning);
        
        this.client.logMessage(params);
    }
    
    private MessageParams getMessageParams(String message) {
        MessageParams messageParams = new MessageParams();
        messageParams.setMessage(message);
        
        return messageParams;
    }
}
