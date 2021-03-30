package org.lsp.server.core.completion;

import io.ballerina.compiler.api.symbols.Documentation;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.Position;
import org.lsp.server.api.completion.BalCompletionResolveContext;

import java.util.List;
import java.util.Map;

public class CompletionItemResolver {
    private static final String DOCUMENTATION = "";

    public static CompletionItem resolve(BalCompletionResolveContext
                                                 context) {
        CompletionItemCapabilities capabilities =
                context.clientCapabilities().getCompletionItem();
        List<String> properties =
                capabilities.getResolveSupport().getProperties();
        if (!properties.contains(DOCUMENTATION)) {
//            return unresolved;
            return null;
        }

        CompletionItem unresolved = context.unresolved();
        Map<String, Object> data =
                (Map<String, Object>) unresolved.getData();
        String alias = (String) data.get("alias");
        String symbolName = (String) data.get("symbolName");
        Position position = (Position) data.get("position");

        Documentation symbolDocumentation =
                getSymbolDocumentation(alias, symbolName, position);

//        MarkupContent documentation =
//                CommonUtils.getMarkupContent(symbolDocumentation);
        CompletionItem clone = clone(unresolved);
//        clone.setDocumentation(documentation);

        return clone;
    }

    private static Documentation getSymbolDocumentation(String alias, String symbolName, Position position) {
        return null;
    }

    private static CompletionItem clone(CompletionItem completionItem) {
        return new CompletionItem();
    }
}
