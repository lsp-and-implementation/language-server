package org.lsp.server.core.completion;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.lsp.server.api.context.BalCompletionContext;

public enum StatementCompletionItem {
    IF_BLOCK(StatementCompletionItemBuilder.getIfStatement()),
    WHILE_BLOCK(StatementCompletionItemBuilder.getWhileStatement());

    private final StatementCompletionItemBuilder.StatementBlock statementBlock;
    StatementCompletionItem(StatementCompletionItemBuilder.StatementBlock statementBlock) {
        this.statementBlock = statementBlock;
    }
    
    CompletionItem get(BalCompletionContext context) {
        boolean snippetSupport = context.clientCapabilities().getCompletionItem().getSnippetSupport();
        String insertText =  snippetSupport ? this.statementBlock.getSnippet()
                : this.statementBlock.getPlainText();
        
        CompletionItem completionItem = new CompletionItem();
        completionItem.setKind(CompletionItemKind.Snippet);
        completionItem.setInsertText(insertText);
        completionItem.setLabel(this.statementBlock.getLabel());
        
        return completionItem;
    }
}
