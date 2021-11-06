package com.lspandimpl.server.core.completion;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lspandimpl.server.api.context.BalCompletionResolveContext;
import com.lspandimpl.server.core.completion.resolve.AutoImportTextEditData;
import com.lspandimpl.server.core.completion.utils.TextEditGenerator;
import com.lspandimpl.server.core.utils.CommonUtils;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

public class CompletionItemResolver {
    private CompletionItemResolver() {
    }

    private static final Gson gson = new Gson();

    public static CompletionItem
    resolve(BalCompletionResolveContext context) {
        CompletionItem unresolved = context.unresolved();
        Optional<AutoImportTextEditData> data = getAutoImportTextEditData(unresolved.getData());
        if (data.isEmpty()) {
            return unresolved;
        }

        // Create a clone for the unresolved CompletionItem
        CompletionItem clone = clone(unresolved);
        Path path = CommonUtils.uriToPath(data.get().getUri());
        SyntaxTree syntaxTree = context.compilerManager().getSyntaxTree(path).orElseThrow();
        // Analyze the syntax tree and generate the text edit
        TextEdit textEdit = TextEditGenerator.getAutoImport(data.get().getImportStatement(), syntaxTree);
        clone.setAdditionalTextEdits(Collections.singletonList(textEdit));

        return clone;
    }
    
    private static CompletionItem clone(CompletionItem from) {
        CompletionItem clone = new CompletionItem();
        clone.setInsertText(from.getInsertText());
        clone.setLabel(from.getLabel());
        clone.setSortText(from.getSortText());


        return from;
    }

    private static Optional<AutoImportTextEditData> getAutoImportTextEditData(Object jsonObject) {
        if (!(jsonObject instanceof JsonObject)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(gson.fromJson((JsonObject) jsonObject, AutoImportTextEditData.class));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
