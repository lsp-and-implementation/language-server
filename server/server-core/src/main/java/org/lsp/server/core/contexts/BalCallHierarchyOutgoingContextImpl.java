package org.lsp.server.core.contexts;

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import org.eclipse.lsp4j.CallHierarchyItem;
import org.eclipse.lsp4j.Position;
import org.lsp.server.api.context.BalCallHierarchyOutgoingContext;
import org.lsp.server.api.context.BalTextDocumentContext;
import org.lsp.server.api.context.LSContext;
import org.lsp.server.core.utils.CommonUtils;
import org.lsp.server.core.utils.ContextEvaluator;

import java.nio.file.Path;

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
