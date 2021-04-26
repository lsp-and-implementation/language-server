package org.lsp.server.core.completion;

import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertReplaceEdit;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.lsp.server.api.context.BalCompletionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FunctionBodyNodeContextProvider6_7 extends
        BalCompletionProviderImpl<FunctionBodyBlockNode> {
    @Override
    public List<CompletionItem>
    getCompletions(FunctionBodyBlockNode node,
                   BalCompletionContext context) {
        List<CompletionItem> items = new ArrayList<>();

        Optional<VariableSymbol> varSymbol =
                this.getVarForFieldAccess(context);
        if (varSymbol.isEmpty()) {
            return items;
        }
        // Fill the forEach snippet
        if (isArrayTypeVar(varSymbol.get())) {
            items.add(getForEachSnippet(context, varSymbol.get()));
        }

        // Extract and fill the langlib methods
        List<FunctionSymbol> langLibMethods =
                varSymbol.get().typeDescriptor().langLibMethods();
        items.addAll(this.convert(langLibMethods, context));

        return items;
    }

    @Override
    public void sort(FunctionBodyBlockNode node, BalCompletionContext context, List<CompletionItem> items) {
        
    }

    @Override
    public void sort(FunctionBodyBlockNode node, BalCompletionContext context, CompletionItem item, Symbol symbol) {

    }

    private CompletionItem
    getForEachSnippet(BalCompletionContext context,
                      VariableSymbol symbol) {
        SimpleNameReferenceNode nameRef =
                (SimpleNameReferenceNode) context.getNodeAtCursor();
        String varName = nameRef.name().text();
        String typeSig = symbol.typeDescriptor().signature();
        CompletionItem item = new CompletionItem();
        boolean snippetSupport = context.clientCapabilities()
                .getCompletionItem().getInsertReplaceSupport();
        String template;
        if (snippetSupport) {
            item.setInsertTextFormat(InsertTextFormat.Snippet);
            template = "foreach %s ${1:item} in %s {}";
        } else {
            item.setInsertTextFormat(InsertTextFormat.PlainText);
            template = "foreach %s item in %s {}";
        }
        String insertText =
                String.format(template, typeSig, varName);

        boolean insertReplaceSupport = context.clientCapabilities()
                .getCompletionItem().getInsertReplaceSupport();

        if (insertReplaceSupport) {
            InsertReplaceEdit edit =
                    getInsertReplaceEdit(context, nameRef, insertText);
            item.setTextEdit(Either.forRight(edit));
        } else {
            TextEdit edit = getTextEdit(context, nameRef, insertText);
            item.setTextEdit(Either.forLeft(edit));
        }
        return item;
    }

    private TextEdit getTextEdit(BalCompletionContext context, SimpleNameReferenceNode nameRef, String insertText) {
        return null;
    }

    private InsertReplaceEdit
    getInsertReplaceEdit(BalCompletionContext context,
                         SimpleNameReferenceNode nameRef,
                         String newText) {
        LineRange lineRange = nameRef.lineRange();
        Position cursor = context.getCursorPosition();
        // Both insert and replace has been set for the same range
        // Depending on the use-case these can be different
        Position start = new Position(
                lineRange.startLine().line(),
                lineRange.startLine().offset());
        Position end = new Position(
                cursor.getLine(),
                cursor.getCharacter());
        Range range = new Range(start, end);

        InsertReplaceEdit edit = new InsertReplaceEdit();
        edit.setNewText(newText);
        edit.setInsert(range);
        edit.setReplace(range);

        return edit;
    }

    @Override
    public boolean enabled() {
        return true;
    }

    private boolean isArrayTypeVar(VariableSymbol symbol) {
        return symbol.typeDescriptor().typeKind() == TypeDescKind.ARRAY;
    }

    private Optional<VariableSymbol> getVarForFieldAccess(BalCompletionContext context) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (nodeAtCursor.kind() == SyntaxKind.FIELD_ACCESS) {
            ExpressionNode expression = ((FieldAccessExpressionNode) nodeAtCursor).expression();
            if (expression.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                String text = ((SimpleNameReferenceNode) expression).name().text();
                return context.visibleSymbols().stream()
                        .filter(symbol -> symbol.getName().isPresent()
                                && symbol.getName().get().equals(text) && symbol.kind() == SymbolKind.VARIABLE)
                        .map(symbol -> (VariableSymbol) symbol)
                        .findFirst();
            }
        }

        return Optional.empty();
    }
}
