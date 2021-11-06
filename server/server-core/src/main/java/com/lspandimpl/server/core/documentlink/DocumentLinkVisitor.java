package com.lspandimpl.server.core.documentlink;

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.tools.text.LinePosition;
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
        // Here we replace the double quotes at the start and end of the token.
        // Also we modify the line range to match the url's range after replacing the quotes
        String text = basicLiteralNode.literalToken().text().replace("\"", "");
        LineRange tokenLineRange = basicLiteralNode.literalToken().lineRange();
        LinePosition start = LinePosition.from(tokenLineRange.startLine().line(),
                tokenLineRange.startLine().offset() + 1);
        LinePosition end = LinePosition.from(tokenLineRange.endLine().line(),
                tokenLineRange.endLine().offset() - 1);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            linkRanges.put(LineRange.from(tokenLineRange.filePath(), start, end), text);
        }
    }

    public Map<LineRange, String> getLinkRanges() {
        return linkRanges;
    }
}
