package org.lsp.server.core.completion;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.lsp.server.api.context.BalCompletionContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionBodyNodeContextProvider6_9 extends
        BalCompletionProviderImpl<FunctionBodyBlockNode> {
    private final String lineSeparator = System.lineSeparator();

    public FunctionBodyNodeContextProvider6_9() {
        super(FunctionBodyBlockNode.class);
    }

    @Override
    public List<CompletionItem>
    getCompletions(FunctionBodyBlockNode node,
                   BalCompletionContext context) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (this.isInQNameReference(context)) {
            /*
            Triggered when typed
            moduleName:<cursor>
             */
            QualifiedNameReferenceNode nameRef =
                    (QualifiedNameReferenceNode) nodeAtCursor;
//            List<Symbol> symbols = CommonUtils.getModuleSymbol(nameRef);
//            return this.prepareCompletionItems(nameRef, symbols, context);
        }
        return Collections.emptyList();
    }

    @Override
    public void sort(FunctionBodyBlockNode node, BalCompletionContext context, List<CompletionItem> items) {
        
    }

    @Override
    public void sort(FunctionBodyBlockNode node, BalCompletionContext context, CompletionItem item, Symbol symbol) {

    }

    private List<CompletionItem>
    prepareCompletionItems(QualifiedNameReferenceNode nameRef,
                          List<Symbol> symbols,
                          BalCompletionContext context) {
        List<CompletionItem> items = new ArrayList<>();
        for (Symbol symbol : symbols) {
            CompletionItem item = new CompletionItem();
            Map<String, Object> data = new HashMap<>();
//            ...
            // Set the module alias as metadata
            data.put("moduleAlias", nameRef.identifier().text());
            data.put("symbolName", nameRef.identifier().text());
            data.put("position", context.getCursorPosition());
//            ...
            item.setData(data);
            
            items.add(item);
        }
        
        return items;
    }

    private boolean isInQNameReference(BalCompletionContext context) {
        return true;
    }

    private Range getAutoImportRange(BalCompletionContext context) {
        return null;
    }

    private TextEdit getAutoImportTextEdit(String ballerina, String io, Range importStmtRange) {
        TextEdit e = new TextEdit();
        return null;
    }

    @Override
    public boolean enabled() {
        return true;
    }
}
