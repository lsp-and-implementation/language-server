package org.lsp.server.core;

import org.eclipse.lsp4j.jsonrpc.services.JsonDelegate;
import org.eclipse.lsp4j.services.LanguageServer;
import org.lsp.server.core.extensions.services.parser.BallerinaParserService;

public interface BalExtendedLanguageServer extends LanguageServer {
    @JsonDelegate
    BallerinaParserService getBallerinaParserService();
}
