package org.lsp.server.api.completion;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.Token;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.lsp.server.api.BaseOperationContext;

import java.util.List;

public interface BalCompletionResolveContext extends BaseOperationContext {
    List<Symbol> visibleSymbols();
    
    CompletionItem unresolved();

    CompletionCapabilities clientCapabilities();

    Position getCursorPosition();
}
