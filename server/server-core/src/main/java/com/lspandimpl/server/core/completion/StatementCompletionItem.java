package com.lspandimpl.server.core.completion;

import com.lspandimpl.server.api.context.BalCompletionContext;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;

public enum StatementCompletionItem {
    IF_BLOCK(StatementCompletionItemBuilder.getIfStatement()),
    WHILE_BLOCK(StatementCompletionItemBuilder.getWhileStatement());

    private final StatementCompletionItemBuilder.StatementBlock statementBlock;

    StatementCompletionItem(StatementCompletionItemBuilder.StatementBlock statementBlock) {
        this.statementBlock = statementBlock;
    }

    CompletionItem get(BalCompletionContext context) {
        boolean snippetSupport = context.clientCapabilities().getTextDocument()
                .getCompletion().getCompletionItem().getSnippetSupport();
        CompletionItem item = new CompletionItem();
        if (snippetSupport) {
            item.setInsertText(this.statementBlock.getSnippet());
            item.setInsertTextFormat(InsertTextFormat.Snippet);
        } else {
            item.setInsertText(this.statementBlock.getPlainText());
            item.setInsertTextFormat(InsertTextFormat.PlainText);
        }
        item.setLabel(this.statementBlock.getLabel());

        return item;
    }
}
