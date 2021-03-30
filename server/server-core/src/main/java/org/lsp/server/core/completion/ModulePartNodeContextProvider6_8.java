package org.lsp.server.core.completion;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.lsp.server.api.completion.BalCompletionContext;
import org.lsp.server.core.completion.utils.SnippetBlock;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModulePartNodeContextProvider6_8 extends
        BalCompletionProviderImpl<ModulePartNode> {
    private final String lineSeparator = System.lineSeparator();
    @Override
    public List<CompletionItem>
    getCompletions(ModulePartNode node,
                   BalCompletionContext context) {
        List<CompletionItem> items = new ArrayList<>(this.getTypeCompletionItems(context));
        items.add(getMainFunctionSnippet(context));

        return items;
    }

    private CompletionItem getMainFunctionSnippet(BalCompletionContext context) {
        CompletionItem item = new CompletionItem();
        String template = "public function main() {" + lineSeparator
                + "\tio:println(\"Hello World!!\");"
                + lineSeparator + "}";
        item.setInsertText(template);
        item.setLabel("main function");
        if (isModuleImported()) {
            /*
            Range starts from the last import statement if there are 
            other imports, otherwise 0,0 is chosen
             */
            Range range = getAutoImportRange(context);
            
            TextEdit autoImport =
                    getAutoImportTextEdit("ballerina", "io", range);
            item.setAdditionalTextEdits(
                    Collections.singletonList(autoImport));
        }
        
        return item;
    }

    private boolean isModuleImported() {
        return false;
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

    @Override
    public void sort(ModulePartNode node,
                     BalCompletionContext context) {
        // TODO: implement
    }
}
