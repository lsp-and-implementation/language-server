package com.lspandimpl.server.core.linkedediting;

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.XMLElementNode;
import io.ballerina.compiler.syntax.tree.XMLEndTagNode;
import io.ballerina.compiler.syntax.tree.XMLStartTagNode;
import org.eclipse.lsp4j.LinkedEditingRanges;
import org.eclipse.lsp4j.Range;
import com.lspandimpl.server.api.context.BalLinkedEditingRangeContext;
import com.lspandimpl.server.core.AbstractProvider;

import java.util.ArrayList;
import java.util.List;

public class LinkedEditingRangeProvider extends AbstractProvider {
    private LinkedEditingRangeProvider() {
    }
    
    public static LinkedEditingRanges getLinkedEditingRanges(BalLinkedEditingRangeContext context) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (nodeAtCursor.kind() != SyntaxKind.XML_SIMPLE_NAME) {
            return null;
        }
        NonTerminalNode parent = nodeAtCursor.parent();
        XMLStartTagNode startTag;
        XMLEndTagNode endTag;
        // Capture the start and the end tags
        if (parent.kind() == SyntaxKind.XML_ELEMENT_START_TAG) {
            startTag = (XMLStartTagNode) parent;
            endTag = ((XMLElementNode) parent.parent()).endTag();
        } else if (parent.kind() == SyntaxKind.XML_ELEMENT_END_TAG) {
            startTag = ((XMLElementNode) parent.parent()).startTag();
            endTag = (XMLEndTagNode) parent;
        } else {
            return null;
        }
    
        LinkedEditingRanges editingRanges = new LinkedEditingRanges();
    
        List<Range> ranges = new ArrayList<>();
        ranges.add(toRange(startTag.name().lineRange()));
        ranges.add(toRange(endTag.name().lineRange()));
    
        editingRanges.setRanges(ranges);
    
        return editingRanges;
    }
}
