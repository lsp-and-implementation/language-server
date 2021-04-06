package org.lsp.server.api;

public interface ClientLogManager {
    /**
     * Log an Info message to the client.
     *
     * @param message {@link String}
     */
    void publishInfo(String message);

    /**
     * Log a Log message to the client.
     *
     * @param message {@link String}
     */
    void publishLog(String message);

    /**
     * Log an Error message to the client.
     *
     * @param message {@link String}
     */
    void publishError(String message);

    /**
     * Log a Warning message to the client.
     *
     * @param message {@link String}
     */
    void publishWarning(String message);
    
    void showErrorMessage(String message);
}
