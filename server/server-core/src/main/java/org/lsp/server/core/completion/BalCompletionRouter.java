package org.lsp.server.core.completion;

import io.ballerina.compiler.syntax.tree.Node;
import org.eclipse.lsp4j.CompletionItem;
import org.lsp.server.api.context.BalCompletionContext;
import org.lsp.server.api.context.BalCompletionProvider;
import org.lsp.server.core.utils.ContextEvaluator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BalCompletionRouter {
    private static final HashMap<Class<?>, BalCompletionProvider> completionProviders;
    static {
        completionProviders = new HashMap<>();
        FunctionBodyNodeContextProvider functionBodyProvider = new FunctionBodyNodeContextProvider();
        ModulePartNodeContextProvider6_8 modulePartProvider = new ModulePartNodeContextProvider6_8();
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
