package com.lspandimpl.server.api;

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

    /**
     * Show an error message to the client.
     *
     * @param message message to be shown
     */
    void showErrorMessage(String message);

    /**
     * Show an info message to the client.
     *
     * @param message message to be shown
     */
    void showInfoMessage(String message);

    /**
     * Show a log message to the client.
     *
     * @param message message to be shown
     */
    void showLogMessage(String message);
}
