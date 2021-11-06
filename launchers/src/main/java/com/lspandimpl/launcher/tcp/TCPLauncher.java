package com.lspandimpl.launcher.tcp;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import com.lspandimpl.server.core.BalLanguageServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TCPLauncher {

    public static void main(String[] args) {
        try {
            Socket clientSocket = new Socket("127.0.0.1", 9925);
            startServer(clientSocket.getInputStream(), clientSocket.getOutputStream());
        } catch (IOException | InterruptedException | ExecutionException e) {
            // Failed to start the server
        }
    }

    public static void startServer(InputStream in, OutputStream out)
            throws InterruptedException, ExecutionException {
        BalLanguageServer server = new BalLanguageServer();
        Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out);
        server.connect(launcher.getRemoteProxy());
        Future<?> startListening = launcher.startListening();
        startListening.get();
    }
}
