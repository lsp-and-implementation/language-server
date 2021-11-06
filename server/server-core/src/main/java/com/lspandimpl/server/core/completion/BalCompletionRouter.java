package com.lspandimpl.server.core.completion;

import com.lspandimpl.server.api.context.BalCompletionContext;
import io.ballerina.compiler.syntax.tree.Node;
import org.eclipse.lsp4j.CompletionItem;
import com.lspandimpl.server.api.context.BalCompletionProvider;
import com.lspandimpl.server.core.utils.ContextEvaluator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BalCompletionRouter {
    private static final HashMap<Class<?>, BalCompletionProvider> completionProviders;
    static {
        completionProviders = new HashMap<>();
        FunctionBodyNodeContextProvider functionBodyProvider = new FunctionBodyNodeContextProvider();
        ModulePartNodeContextProvider modulePartProvider = new ModulePartNodeContextProvider();
        completionProviders.put(functionBodyProvider.attachmentPoint(), functionBodyProvider);
        completionProviders.put(modulePartProvider.attachmentPoint(), modulePartProvider);
    }
    public static List<CompletionItem> compute(BalCompletionContext ctx) {
        ContextEvaluator.fillTokenInfoAtCursor(ctx);
        Node node = ctx.getNodeAtCursor();
        BalCompletionProvider<Node> provider = null;
        
        while (node != null) {
            if (completionProviders.containsKey(node.getClass())) {
                provider = completionProviders.get(node.getClass());
                break;
            }
            node = node.parent();
        }
        
        return (provider == null)  ? Collections.emptyList() : provider.getCompletions(node, ctx);
    }
}
