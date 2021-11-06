package com.lspandimpl.server.core;

import org.eclipse.lsp4j.jsonrpc.services.JsonDelegate;
import org.eclipse.lsp4j.services.LanguageServer;
import com.lspandimpl.server.core.extensions.services.parser.BallerinaParserService;

public interface BalExtendedLanguageServer extends LanguageServer {
    @JsonDelegate
    BallerinaParserService getBallerinaParserService();
}
