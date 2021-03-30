package org.lsp.server.api.completion;

import io.ballerina.compiler.syntax.tree.Node;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;

public interface BalCompletionProvider<T extends Node> {
    List<CompletionItem> getCompletions(T node, BalCompletionContext context);
    
    void sort(T node, BalCompletionContext context);
    
    boolean enabled();
}
