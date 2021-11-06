package com.lspandimpl.server.api.context;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.Node;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;

public interface BalCompletionProvider<T extends Node> {
    List<CompletionItem> getCompletions(T node, BalCompletionContext context);
    
    void sort(T node, BalCompletionContext context, List<CompletionItem> items);
    
    void sort(T node, BalCompletionContext context, CompletionItem item, Symbol symbol);
    
    boolean enabled();
    
    Class<? extends Node> attachmentPoint();
}
