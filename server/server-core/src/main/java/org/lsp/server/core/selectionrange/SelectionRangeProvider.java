package org.lsp.server.core.selectionrange;

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SelectionRange;
import org.lsp.server.api.context.BalSelectionRangeContext;
import org.lsp.server.core.AbstractProvider;
import org.lsp.server.core.utils.ContextEvaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SelectionRangeProvider extends AbstractProvider {
    public static List<SelectionRange> getSelectionRange(BalSelectionRangeContext context) {
        Optional<SyntaxTree> syntaxTree = context.compilerManager().getSyntaxTree(context.getPath());
        if (syntaxTree.isEmpty()) {
            return Collections.emptyList();
        }
        List<SelectionRange> selectionRanges = new ArrayList<>();
        for (Position position : context.positions()) {
            NonTerminalNode nodeAtPosition = ContextEvaluator.nodeAtPosition(position, context);
            NonTerminalNode parent = nodeAtPosition.parent();
            SelectionRange selectionRange = new SelectionRange();
            Range range = toRange(parent.lineRange());
            selectionRange.setRange(range);
            if (parent.parent() != null) {
                SelectionRange parentSelectionRange = new SelectionRange();
                Range parentRange = toRange(parent.parent().lineRange());
                parentSelectionRange.setRange(parentRange);
                selectionRange.setParent(parentSelectionRange);
            }
            selectionRanges.add(selectionRange);
        }
        
        return selectionRanges;
    }
}
