package com.lspandimpl.server.core.format;

import com.lspandimpl.server.core.utils.CommonUtils;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import com.lspandimpl.server.api.context.BalPosBasedContext;
import com.lspandimpl.server.api.context.BaseOperationContext;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class FormatProvider {
    public static List<TextEdit> format(BaseOperationContext context,
                                        DocumentFormattingParams params) {
        Path path = CommonUtils.uriToPath(params.getTextDocument().getUri());
        SyntaxTree syntaxTree = context.compilerManager()
                .getSyntaxTree(path).orElseThrow();
        try {
            // Current ballerina formatter has default behaviour
            // Based on the formatter, formatting options can read and utilize
            // FormattingOptions options = params.getOptions();
            String formattedSource = Formatter.format(syntaxTree).toSourceCode();
            LinePosition eofPos = syntaxTree.rootNode().lineRange().endLine();
            // Replace the full document context
            Range range = new Range(new Position(0, 0),
                    new Position(eofPos.line() + 1, eofPos.offset()));
            TextEdit textEdit = new TextEdit(range, formattedSource);

            return Collections.singletonList(textEdit);
        } catch (FormatterException e) {
            return Collections.emptyList();
        }
    }

    public static List<TextEdit> formatRange(BaseOperationContext context,
                                             DocumentRangeFormattingParams params) {
        Path path = CommonUtils.uriToPath(params.getTextDocument().getUri());
        SyntaxTree syntaxTree = context.compilerManager()
                .getSyntaxTree(path).orElseThrow();
        try {
            // Current ballerina formatter has default behaviour
            // Based on the formatter, formatting options can read and utilize
            // FormattingOptions options = params.getOptions();
            Range range = params.getRange();
            LinePosition startPos = LinePosition.from(
                    range.getStart().getLine(),
                    range.getStart().getCharacter());
            LinePosition endPos = LinePosition.from(
                    range.getEnd().getLine(),
                    range.getEnd().getCharacter());

            LineRange lineRange = LineRange.from(
                    syntaxTree.filePath(),
                    startPos, endPos);
            SyntaxTree formattedTree
                    = Formatter.format(syntaxTree, lineRange);

            LinePosition eofPos = syntaxTree.rootNode().lineRange().endLine();
            Range updateRange = new Range(new Position(0, 0),
                    new Position(eofPos.line() + 1, eofPos.offset()));
            TextEdit textEdit = new TextEdit(updateRange,
                    formattedTree.toSourceCode());

            return Collections.singletonList(textEdit);
        } catch (FormatterException e) {
            return Collections.emptyList();
        }
    }

public static List<TextEdit>
onTypeFormat(BalPosBasedContext context,
             DocumentOnTypeFormattingParams params) {
    Path path = CommonUtils
            .uriToPath(params.getTextDocument().getUri());
    SyntaxTree syntaxTree = context.compilerManager()
            .getSyntaxTree(path).orElseThrow();
    try {
        // Current ballerina formatter has default behaviour
        // Based on the formatter, formatting options can read and utilize
        // FormattingOptions options = params.getOptions();
        LineRange lRange;
        NonTerminalNode nodeToFormat = getNodeToFormat(context.getNodeAtCursor());
        if ((params.getCh().equals("}")
                && nodeToFormat.kind() == SyntaxKind.FUNCTION_DEFINITION)
                || (params.getCh().equals(";")
                && nodeToFormat.kind() == SyntaxKind.LOCAL_VAR_DECL)) {
            lRange = nodeToFormat.lineRange();
        } else {
            return Collections.emptyList();
        }

        SyntaxTree formattedTree
                = Formatter.format(syntaxTree, lRange);

        LinePosition eofPos = syntaxTree.rootNode().lineRange().endLine();
        Range updateRange = new Range(new Position(0, 0),
                new Position(eofPos.line() + 1, eofPos.offset()));
        TextEdit textEdit = new TextEdit(updateRange,
                formattedTree.toSourceCode());

        return Collections.singletonList(textEdit);
    } catch (FormatterException e) {
        return Collections.emptyList();
    }
}

    private static NonTerminalNode getNodeToFormat(NonTerminalNode node) {
        if (node.kind() == SyntaxKind.FUNCTION_BODY_BLOCK) {
            return node.parent();
        }

        return node;
    }

    private static Position toPosition(LinePosition linePosition) {
        Position position = new Position();
        position.setLine(linePosition.line());
        position.setCharacter(linePosition.offset());

        return position;
    }

    private static Range toRange(LineRange lineRange) {
        Range range = new Range();
        range.setStart(toPosition(lineRange.startLine()));
        range.setEnd(toPosition(lineRange.endLine()));

        return range;
    }
}
