package com.lspandimpl.server.core.hover;

import com.lspandimpl.server.api.context.BalHoverContext;
import com.lspandimpl.server.core.utils.CommonUtils;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.EnumSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.nio.file.Path;
import java.util.Optional;

public class HoverProvider {
    private HoverProvider() {
    }

    public static Hover getHover(BalHoverContext context) {
        Hover hover = new Hover();
        Path path = context.getPath();
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        Optional<Symbol> symbol = context.compilerManager()
                .getSemanticModel(path).orElseThrow()
                .symbol(nodeAtCursor);

        if (symbol.isEmpty()) {
            return null;
        }
        MarkupContent markupContent = getMarkupContent(symbol.get());
        hover.setContents(Either.forRight(markupContent));
        
        hover.setRange(toRange(nodeAtCursor.lineRange()));
        
        return hover;
    }

    private static MarkupContent getMarkupContent(Symbol symbol) {
        Optional<Documentation> documentation;
        MarkupContent markupContent = new MarkupContent();
        StringBuilder content = new StringBuilder();
        markupContent.setKind(MarkupKind.MARKDOWN);
        switch (symbol.kind()) {
            case FUNCTION:
                documentation = ((FunctionSymbol) symbol).documentation();
                break;
            case ENUM:
                documentation = ((EnumSymbol) symbol).documentation();
                break;
            default:
                documentation = Optional.empty();
                break;
        }

        documentation.ifPresent(value -> content.append("## Description")
                .append(CommonUtils.MD_LINE_SEPARATOR)
                .append(value.description().orElse(""))
                .append(CommonUtils.MD_LINE_SEPARATOR));
        
        switch (symbol.kind()) {
            case FUNCTION:
                content.append(((FunctionSymbol) symbol).typeDescriptor().signature());
                break;
            case ENUM:
                content.append(((EnumSymbol) symbol).typeDescriptor().signature());
                break;
            default:
                break;
        }
        
        markupContent.setValue(content.toString());
        
        return markupContent;
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
