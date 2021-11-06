package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.BalCallHierarchyOutgoingContext;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import org.eclipse.lsp4j.CallHierarchyItem;
import com.lspandimpl.server.api.context.LSContext;
import com.lspandimpl.server.core.utils.ContextEvaluator;

public class BalCallHierarchyOutgoingContextImpl extends BalTextDocumentContextImpl implements BalCallHierarchyOutgoingContext {
    private final CallHierarchyItem item;
    private NonTerminalNode nodeForItem;
    
    public BalCallHierarchyOutgoingContextImpl(LSContext serverContext, CallHierarchyItem item) {
        super(serverContext, item.getUri());
        this.item = item;
    }

    @Override
    public NonTerminalNode nodeForItem() {
        if (nodeForItem == null) {
            nodeForItem = ContextEvaluator.nodeAtPosition(item.getRange().getStart(), this);
        }
        
        return nodeForItem;
    }
}
