package org.lsp.server.core.completion;

import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionItemTag;
import org.lsp.server.api.completion.BalCompletionContext;
import org.lsp.server.api.completion.BalCompletionProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BalCompletionProviderImpl<T extends Node> implements BalCompletionProvider<T> {
    @Override
    public boolean enabled() {
        return false;
    }
    
    protected List<CompletionItem>
    getTypeCompletionItems(BalCompletionContext context) {
        List<CompletionItem> completionItems = new ArrayList<>();
        for (Symbol symbol : context.visibleSymbols()) {
            if (symbol.kind() == SymbolKind.TYPE_DEFINITION) {
                TypeDefinitionSymbol tDesc = (TypeDefinitionSymbol) symbol;
                CompletionItem item = new CompletionItem();
                
                item.setKind(CompletionItemKind.TypeParameter);
                item.setLabel(symbol.getName().get());
                item.setInsertText(symbol.getName().get());
                // Set the type signature as the detail
                item.setDetail(tDesc.typeDescriptor().signature());
                
                List<AnnotationSymbol> annotations = tDesc.annotations();
                boolean deprecated = annotations.stream()
                        .anyMatch(annot ->annot.getName()
                                .orElse("").equals("deprecated"));
                if (deprecated) {
                    item.setTags(Collections
                            .singletonList(CompletionItemTag.Deprecated));
                }
                completionItems.add(item);
            }
        }
        
        return completionItems;
    }

    /**
     * Convert the symbols to the completion items.
     *
     * @param symbols symbols to be convert
     * @param context Completion context
     * @return {@link List} of completions
     */
    protected List<CompletionItem>
    convert(List<? extends Symbol> symbols, BalCompletionContext context) {
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
}
