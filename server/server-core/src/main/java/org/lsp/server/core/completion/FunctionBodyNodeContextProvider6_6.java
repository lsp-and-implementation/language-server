package org.lsp.server.core.completion;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import org.eclipse.lsp4j.CompletionItem;
import org.lsp.server.api.completion.BalCompletionContext;
import org.lsp.server.core.completion.utils.SnippetBlock;

import java.util.ArrayList;
import java.util.List;

public class FunctionBodyNodeContextProvider6_6 extends
        BalCompletionProviderImpl<FunctionBodyBlockNode> {
    @Override
    public List<CompletionItem>
    getCompletions(FunctionBodyBlockNode node,
                   BalCompletionContext context) {
        List<CompletionItem> items = new ArrayList<>();
        // Insert text in Snippet format
        String ifSnippet = "if (${1:true}) {${2}}";
        // Insert text in PlainText format
        String ifPlainText = "if (true) {}";
        String label = "if";
        SnippetBlock ifBlock =
                new SnippetBlock(label, ifSnippet, ifPlainText);
        items.add(ifBlock.build(context));

        return items;
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
}
