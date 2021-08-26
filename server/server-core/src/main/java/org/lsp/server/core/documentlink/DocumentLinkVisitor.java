package org.lsp.server.core.documentlink;

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.tools.text.LineRange;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentLinkVisitor extends NodeVisitor {
    private final String urlRegex = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    Pattern pattern = Pattern.compile(urlRegex);
    private final Map<LineRange, String> linkRanges = new HashMap<>();

    @Override
    public void visit(BasicLiteralNode basicLiteralNode) {
        String text = basicLiteralNode.literalToken().text();
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            linkRanges.put(basicLiteralNode.literalToken().lineRange(), text);
        }
    }

    public Map<LineRange, String> getLinkRanges() {
        return linkRanges;
    }
}
