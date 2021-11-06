package com.lspandimpl.server.api.context;

import io.ballerina.compiler.api.symbols.Symbol;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;

import java.util.List;

public interface BalCompletionResolveContext extends BalTextDocumentContext {
    List<Symbol> visibleSymbols();
    
    CompletionItem unresolved();

    Position getCursorPosition();
}
