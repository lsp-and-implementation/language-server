package com.lspandimpl.server.core.utils;

import com.lspandimpl.server.api.context.BaseOperationContext;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;
import java.util.Optional;

public class TextModifierUtil {
    
    public static Optional<TextEdit> withEndingNewLine(Path path, BaseOperationContext context) {
        Document document = context.compilerManager().getDocument(path).orElseThrow();
        String sourceCode = document.syntaxTree().toSourceCode();
        if (!sourceCode.endsWith(CommonUtils.LINE_SEPARATOR)) {
            TextEdit textEdit = new TextEdit();
            ModulePartNode modulePartNode = document.syntaxTree().rootNode();
            textEdit.setNewText(sourceCode + CommonUtils.LINE_SEPARATOR);
            
            LinePosition endLine = modulePartNode.lineRange().endLine();
            Range range = new Range();
            range.setStart(new Position(0, 0));
            range.setEnd(new Position(endLine.line(), endLine.offset()));
            
            textEdit.setRange(range);
            return Optional.of(textEdit);
        }
        
        return Optional.empty();
    }
}
