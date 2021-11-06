package com.lspandimpl.server.core.completion;

import com.lspandimpl.server.api.context.BalCompletionContext;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionBodyNodeContextProvider extends
        BalCompletionProviderImpl<FunctionBodyBlockNode> {

    public FunctionBodyNodeContextProvider() {
        super(FunctionBodyBlockNode.class);
    }

    @Override
    public List<CompletionItem>
    getCompletions(FunctionBodyBlockNode node,
                   BalCompletionContext context) {
        
        List<CompletionItem> completionItems = new ArrayList<>();
        List<Symbol> symbols = context.visibleSymbols().stream()
                .filter(s -> s.kind() == SymbolKind.TYPE_DEFINITION
                        || s.kind() == SymbolKind.VARIABLE
                        || s.kind() == SymbolKind.CONSTANT
                        || s.kind() ==SymbolKind.FUNCTION)
                .collect(Collectors.toList());

        completionItems.addAll(this.convert(symbols, context));
        completionItems.addAll(this.getFunctionBodySnippets(context));
        
        return completionItems;
    }

    @Override
    public void sort(FunctionBodyBlockNode node, BalCompletionContext context, List<CompletionItem> items) {
        
    }

    @Override
    public void sort(FunctionBodyBlockNode node, BalCompletionContext context, CompletionItem item, Symbol symbol) {

    }

    @Override
    public boolean enabled() {
        return true;
    }
    
    private List<CompletionItem> getFunctionBodySnippets(BalCompletionContext context) {
        return Arrays.asList(
                StatementCompletionItem.IF_BLOCK.get(context),
                StatementCompletionItem.WHILE_BLOCK.get(context)
        );
    }
}
