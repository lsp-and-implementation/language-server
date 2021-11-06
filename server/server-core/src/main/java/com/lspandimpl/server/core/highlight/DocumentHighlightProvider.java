package com.lspandimpl.server.core.highlight;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.projects.Document;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import com.lspandimpl.server.api.context.BalDocumentHighlightContext;
import com.lspandimpl.server.core.utils.ContextEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DocumentHighlightProvider {
    private DocumentHighlightProvider() {
    }
    
    public static List<DocumentHighlight>
    getHighlight(BalDocumentHighlightContext context) {
        SemanticModel semanticModel = context.compilerManager()
                .getSemanticModel(context.getPath()).orElseThrow();
        Document document = context.compilerManager()
                .getDocument(context.getPath()).orElseThrow();
        Position position = context.getCursorPosition();
        LinePosition linePos = LinePosition.from(position.getLine(),
                position.getCharacter());
        List<Location> references =
                semanticModel.references(document, linePos);
        
        List<DocumentHighlight> highlights = new ArrayList<>();

        for (Location location : references) {
            LinePosition sLine = location.lineRange().startLine();
            LinePosition eLine = location.lineRange().endLine();
            Position start = new Position(sLine.line(), sLine.offset());
            Position end = new Position(eLine.line(), eLine.offset());
            Range range = new Range(start, end);

            DocumentHighlight highlight = new DocumentHighlight();
            highlight.setRange(range);
            if (isWrite(context, location)) {
                highlight.setKind(DocumentHighlightKind.Write);
            } else {
                highlight.setKind(DocumentHighlightKind.Read);
            }
            highlights.add(highlight);
        }
        
        return highlights;
    }
    
    private static boolean isWrite(BalDocumentHighlightContext context, Location location) {
        SemanticModel semanticModel = context.compilerManager()
                .getSemanticModel(context.getPath()).orElseThrow();
        LinePosition startLine = location.lineRange().startLine();
        Position pos = new Position(startLine.line(), startLine.offset() + 1);
        NonTerminalNode nodeAtPosition = ContextEvaluator.nodeAtPosition(pos, context);
        Optional<Symbol> symbol = semanticModel.symbol(nodeAtPosition);
        if (symbol.isEmpty()) {
            return false;
        }
        // TODO: Revamp and improve with a comparator
        LinePosition symbolStart = symbol.get().getLocation().get().lineRange().startLine();
        return symbolStart.line() == startLine.line() && symbolStart.offset() == startLine.offset();
    }
}
