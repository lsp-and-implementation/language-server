package org.lsp.server.core.completion;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import org.eclipse.lsp4j.CompletionItem;
import org.lsp.server.api.completion.BalCompletionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionBodyNodeContextProvider6_5 extends
        BalCompletionProviderImpl<FunctionBodyBlockNode> {
    @Override
    public List<CompletionItem>
    getCompletions(FunctionBodyBlockNode node,
                   BalCompletionContext context) {
        // Filter the type definitions
        List<Symbol> typeDefs = context.visibleSymbols().stream()
                .filter(s -> s.kind() == SymbolKind.TYPE_DEFINITION)
                .collect(Collectors.toList());

        return this.convert(typeDefs, context);
    }

    @Override
    public void sort(FunctionBodyBlockNode node, BalCompletionContext context, List<CompletionItem> items) {
        
    }

    @Override
    public void sort(FunctionBodyBlockNode node, BalCompletionContext context, CompletionItem item, Symbol symbol) {

    }

    /**
     * Convert the symbols to the completion items.
     *
     * @param symbols symbols to be convert
     * @param context Completion context
     * @return {@link List} of completions
     */
    @Override
    protected List<CompletionItem> convert(List<? extends Symbol> symbols, BalCompletionContext context) {
        List<CompletionItem> completionItems = new ArrayList<>();
        for (Symbol symbol : symbols) {
            CompletionItem cItem = new CompletionItem();
            // Set the insert text and the label
            cItem.setInsertText(symbol.getName().get());
            cItem.setLabel(symbol.getName().get());
            completionItems.add(cItem);
        }

        return completionItems;
    }

    @Override
    public boolean enabled() {
        return true;
    }
}
