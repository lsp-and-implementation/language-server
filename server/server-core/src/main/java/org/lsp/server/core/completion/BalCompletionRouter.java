package org.lsp.server.core.completion;

import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.eclipse.lsp4j.CompletionItem;
import org.lsp.server.api.context.BalCompletionContext;
import org.lsp.server.core.utils.ContextEvaluator;

import java.util.Collections;
import java.util.List;

public class BalCompletionRouter {
    
    public static List<CompletionItem> compute(BalCompletionContext ctx) {
        ContextEvaluator.fillTokenInfoAtCursor(ctx);
        NonTerminalNode node = ctx.getNodeAtCursor();

        if (node.kind() == SyntaxKind.MODULE_PART) {
            return new ModulePartNodeContextProvider6_8()
                    .getCompletions((ModulePartNode) node, ctx);
        }
        if (node.kind() == SyntaxKind.FUNCTION_BODY_BLOCK) {
            return new FunctionBodyNodeContextProvider6_5()
                    .getCompletions((FunctionBodyBlockNode) node, ctx);
        }
        
        return Collections.emptyList();
    }
}
