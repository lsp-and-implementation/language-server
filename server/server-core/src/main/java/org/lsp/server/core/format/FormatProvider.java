package org.lsp.server.core.format;

import io.ballerina.compiler.syntax.tree.Node;
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
import org.lsp.server.api.context.BaseOperationContext;
import org.lsp.server.core.utils.CommonUtils;

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

            LineRange lineRange =LineRange.from(
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
    
    public static List<TextEdit> onTypeFormat(BaseOperationContext context,
                                              DocumentOnTypeFormattingParams params) {
        Path path = CommonUtils.uriToPath(params.getTextDocument().getUri());
        SyntaxTree syntaxTree = context.compilerManager()
                .getSyntaxTree(path).orElseThrow();
        try {
            // Current ballerina formatter has default behaviour
            // Based on the formatter, formatting options can read and utilize
            // FormattingOptions options = params.getOptions();
            Range range;
            LineRange lRange;
            if (params.getCh().equals("}")) {
                lRange = getBlock(context, params.getPosition()).lineRange();
            } else if (params.getCh().equals(";")) {
                lRange = getStatement(context, params.getPosition()).lineRange();
            } else {
                return Collections.emptyList();
            }

            LinePosition startLine = lRange.startLine();
            LinePosition endLine = lRange.endLine();
            range = new Range(new Position(startLine.line(), startLine.offset()),
                    new Position(endLine.line(), endLine.offset()));

            LinePosition startPos = LinePosition.from(
                    range.getStart().getLine(),
                    range.getStart().getCharacter());
            LinePosition endPos = LinePosition.from(
                    range.getEnd().getLine(),
                    range.getEnd().getCharacter());

            LineRange lineRange =LineRange.from(
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
    
    private static Node getBlock(BaseOperationContext context, Position position) {
        return null;
    }
    
    private static Node getStatement(BaseOperationContext context, Position position) {
        return null;
    }
}
