package com.lspandimpl.server.core.completion;

import com.lspandimpl.server.api.context.BalCompletionContext;
import com.lspandimpl.server.core.utils.CommonUtils;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemCapabilities;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionItemTag;
import org.eclipse.lsp4j.CompletionItemTagSupportCapabilities;
import org.eclipse.lsp4j.InsertReplaceEdit;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import com.lspandimpl.server.api.context.BalCompletionProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class BalCompletionProviderImpl<T extends Node> implements BalCompletionProvider<T> {
    private final Class<? extends Node> attachmentPoint;

    public BalCompletionProviderImpl(Class<? extends Node> attachmentPoint) {
        this.attachmentPoint = attachmentPoint;
    }

    @Override
    public Class<? extends Node> attachmentPoint() {
        return this.attachmentPoint;
    }

    @Override
    public boolean enabled() {
        return false;
    }

    protected List<CompletionItem> getTypeCompletionItems(BalCompletionContext context) {
        List<CompletionItem> completionItems = new ArrayList<>();
        for (Symbol symbol : context.visibleSymbols()) {
            if (symbol.kind() == SymbolKind.TYPE_DEFINITION) {
                TypeDefinitionSymbol tDesc = (TypeDefinitionSymbol) symbol;
                CompletionItem item = new CompletionItem();

                item.setKind(CompletionItemKind.TypeParameter);
                item.setLabel(symbol.getName().get());
                item.setInsertText(symbol.getName().get());
                // Set the type signature as the detail
                item.setDetail(tDesc.typeDescriptor().signature());

                List<AnnotationSymbol> annotations = tDesc.annotations();
                boolean deprecated = annotations.stream()
                        .anyMatch(annot -> annot.getName()
                                .orElse("").equals("deprecated"));
                if (deprecated) {
                    item.setTags(Collections
                            .singletonList(CompletionItemTag.Deprecated));
                }
                completionItems.add(item);
            }
        }

        return completionItems;
    }

    /**
     * Convert the symbols to the completion items.
     *
     * @param symbols symbols to be convert
     * @param context Completion context
     * @return {@link List} of completions
     */
    protected List<CompletionItem> convert(List<? extends Symbol> symbols,
                                           BalCompletionContext context) {
        List<CompletionItem> completionItems = new ArrayList<>();
        for (Symbol symbol : symbols) {
            if (symbol.getName().isEmpty()) {
                continue;
            }
            CompletionItem cItem = new CompletionItem();
            // Set the insert text/ textEdit and the label
            // this.setInsertText(symbol, context, cItem);
            // this.setTextEdit(symbol, context, cItem);
            this.setInsertReplaceTextEdit(symbol, context, cItem);
            this.setDetail(symbol, context, cItem);
            cItem.setLabel(symbol.getName().get());
            this.setDocumentation(symbol, context, cItem);
            this.setTags(symbol, context, cItem);
            completionItems.add(cItem);
        }

        return completionItems;
    }

    private void setDetail(Symbol symbol,
                           BalCompletionContext context,
                           CompletionItem cItem) {
        String detail;
        switch (symbol.kind()) {
            case FUNCTION:
                Optional<TypeSymbol> tSymbol =
                        ((FunctionSymbol) symbol).typeDescriptor()
                                .returnTypeDescriptor();
                detail = tSymbol.isPresent() ? tSymbol.get().signature() : "()";
                break;
            case TYPE_DEFINITION:
                detail = ((TypeDefinitionSymbol) symbol).typeDescriptor().signature();
                break;
            case VARIABLE:
                detail = ((VariableSymbol) symbol).typeDescriptor().signature();
                break;
            default:
                return;
        }

        cItem.setDetail(detail);
    }

    private void setTags(Symbol symbol,
                         BalCompletionContext context,
                         CompletionItem cItem) {
        List<AnnotationSymbol> annotations;
        switch (symbol.kind()) {
            case CLASS:
                annotations = ((ClassSymbol) symbol).annotations();
                break;
            case FUNCTION:
                annotations = ((FunctionSymbol) symbol).annotations();
                break;
            case TYPE_DEFINITION:
                annotations = ((TypeDefinitionSymbol) symbol).annotations();
                break;
            default:
                annotations = Collections.emptyList();
                break;
        }
        CompletionItemCapabilities itemCapabilities =
                context.clientCapabilities().getTextDocument()
                        .getCompletion().getCompletionItem();
        CompletionItemTagSupportCapabilities tagSupport =
                itemCapabilities.getTagSupport();
        List<CompletionItemTag> supportedTags = tagSupport.getValueSet();

        Optional<AnnotationSymbol> deprecatedAnnotation = annotations.stream()
                .filter(annot -> annot.getName().orElse("").equals("deprecated"))
                .findAny();

        if (deprecatedAnnotation.isPresent() &&
                supportedTags.contains(CompletionItemTag.Deprecated)) {
            cItem.setTags(Collections.singletonList(CompletionItemTag.Deprecated));
        }
    }

    private void setDocumentation(Symbol symbol, BalCompletionContext context, CompletionItem cItem) {
        Optional<Documentation> documentation;

        switch (symbol.kind()) {
            case CLASS:
                documentation = ((ClassSymbol) symbol).documentation();
                break;
            case FUNCTION:
                documentation = ((FunctionSymbol) symbol).documentation();
                break;
            case TYPE_DEFINITION:
                documentation = ((TypeDefinitionSymbol) symbol).documentation();
                break;
            default:
                documentation = Optional.empty();
                break;
        }
        if (documentation.isEmpty() || documentation.get().description().isEmpty()) {
            return;
        }
        CompletionItemCapabilities capabilities =
                context.clientCapabilities().getTextDocument()
                        .getCompletion().getCompletionItem();
        String description = documentation.get().description().get();
        List<String> docFormat = capabilities.getDocumentationFormat();
        Either<String, MarkupContent> itemDocs;
        if (docFormat.contains(MarkupKind.MARKDOWN)) {
            MarkupContent markupContent = new MarkupContent();
            markupContent.setKind(MarkupKind.MARKDOWN);
            markupContent.setValue("## Description " + CommonUtils.MD_LINE_SEPARATOR + description);

            itemDocs = Either.forRight(markupContent);
        } else {
            itemDocs = Either.forLeft(description);
        }

        cItem.setDocumentation(itemDocs);
    }

    private void setInsertText(Symbol symbol, BalCompletionContext context, CompletionItem cItem) {
        CompletionItemCapabilities capabilities = context.clientCapabilities()
                .getTextDocument().getCompletion().getCompletionItem();
        StringBuilder insertTxtBuilder = new StringBuilder(symbol.getName().get());
        InsertTextFormat insertTextFormat;

        if (symbol.kind() == SymbolKind.FUNCTION) {
            insertTxtBuilder.append("(");
            Optional<List<ParameterSymbol>> params = ((FunctionSymbol) symbol).typeDescriptor().params();
            if (params.isPresent() && !params.get().isEmpty() && capabilities.getSnippetSupport()) {
                insertTxtBuilder.append("${1}");
                insertTextFormat = InsertTextFormat.Snippet;
            } else {
                insertTextFormat = InsertTextFormat.PlainText;
            }
            insertTxtBuilder.append(")");
        } else {
            insertTextFormat = InsertTextFormat.PlainText;
        }
        cItem.setInsertText(insertTxtBuilder.toString());
        cItem.setInsertTextFormat(insertTextFormat);
    }

    private void setTextEdit(Symbol symbol, BalCompletionContext context, CompletionItem cItem) {
        CompletionItemCapabilities capabilities = context.clientCapabilities()
                .getTextDocument().getCompletion().getCompletionItem();
        StringBuilder insertTxtBuilder = new StringBuilder(symbol.getName().get());
        InsertTextFormat insertTextFormat;
        TextEdit textEdit = new TextEdit();
        Range range;
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (nodeAtCursor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            LineRange lineRange = ((SimpleNameReferenceNode) nodeAtCursor).name().lineRange();
            range = this.toRange(lineRange);
        } else {
            range = new Range(context.getCursorPosition(), context.getCursorPosition());
        }

        if (symbol.kind() == SymbolKind.FUNCTION) {
            insertTxtBuilder.append("(");
            Optional<List<ParameterSymbol>> params = ((FunctionSymbol) symbol).typeDescriptor().params();
            if (params.isPresent() && !params.get().isEmpty() && capabilities.getSnippetSupport()) {
                insertTxtBuilder.append("${1}");
                insertTextFormat = InsertTextFormat.Snippet;
            } else {
                insertTextFormat = InsertTextFormat.PlainText;
            }
            insertTxtBuilder.append(")");
        } else {
            insertTextFormat = InsertTextFormat.PlainText;
        }

        textEdit.setNewText(insertTxtBuilder.toString());
        textEdit.setRange(range);
        cItem.setTextEdit(Either.forLeft(textEdit));
        cItem.setInsertTextFormat(insertTextFormat);
    }

    private void setInsertReplaceTextEdit(Symbol symbol,
                                          BalCompletionContext context,
                                          CompletionItem cItem) {
        CompletionItemCapabilities capabilities =
                context.clientCapabilities().getTextDocument()
                        .getCompletion().getCompletionItem();
        StringBuilder insertTxtBuilder = new StringBuilder(symbol.getName().get());
        InsertTextFormat insertTextFormat;
        Position cursor = context.getCursorPosition();
        Range insertRange;
        Range replaceRange;
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();

        if (symbol.kind() == SymbolKind.FUNCTION) {
            insertTxtBuilder.append("(");
            Optional<List<ParameterSymbol>> params = ((FunctionSymbol) symbol).typeDescriptor().params();
            if (params.isPresent() && !params.get().isEmpty() && capabilities.getSnippetSupport()) {
                insertTxtBuilder.append("${1}");
                insertTextFormat = InsertTextFormat.Snippet;
            } else {
                insertTextFormat = InsertTextFormat.PlainText;
            }
            insertTxtBuilder.append(")");
        } else {
            insertTextFormat = InsertTextFormat.PlainText;
        }
        cItem.setInsertTextFormat(insertTextFormat);
        if (nodeAtCursor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            LineRange lineRange =
                    ((SimpleNameReferenceNode) nodeAtCursor).name().lineRange();
            Position insertStart =
                    this.toPosition(lineRange.startLine());
            Position insertEnd =
                    new Position(insertStart.getLine(), cursor.getCharacter());
            insertRange = new Range(insertStart, insertEnd);
            replaceRange = this.toRange(lineRange);

            InsertReplaceEdit textEdit = new InsertReplaceEdit();
            textEdit.setNewText(insertTxtBuilder.toString());
            textEdit.setInsert(insertRange);
            textEdit.setReplace(replaceRange);
            cItem.setTextEdit(Either.forRight(textEdit));
        } else {
            cItem.setInsertText(insertTxtBuilder.toString());
        }
    }

    private Position toPosition(LinePosition linePosition) {
        Position position = new Position();
        position.setLine(linePosition.line());
        position.setCharacter(linePosition.offset());

        return position;
    }

    private Range toRange(LineRange lineRange) {
        Range range = new Range();
        range.setStart(toPosition(lineRange.startLine()));
        range.setEnd(toPosition(lineRange.endLine()));

        return range;
    }
}
