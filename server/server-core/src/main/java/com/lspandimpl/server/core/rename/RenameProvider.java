package com.lspandimpl.server.core.rename;

import com.lspandimpl.server.api.context.BalPrepareRenameContext;
import com.lspandimpl.server.api.context.BalRenameContext;
import com.lspandimpl.server.ballerina.compiler.workspace.CompilerManager;
import com.lspandimpl.server.core.utils.CommonUtils;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.AnnotatedTextEdit;
import org.eclipse.lsp4j.ChangeAnnotation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PrepareRenameResult;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RenameProvider {
    public static WorkspaceEdit
    getRename(BalRenameContext context) {
        String newName = context.params().getNewName();
        
        WorkspaceEdit workspaceEdit = new WorkspaceEdit(getDocumentChanges(context, newName));
        // set the change annotations
        Map<String, ChangeAnnotation> changeAnnotations = getChangeAnnotations();
        workspaceEdit.setChangeAnnotations(changeAnnotations);

        return workspaceEdit;
    }

    private static List<Either<TextDocumentEdit, ResourceOperation>>
    getDocumentChanges(BalRenameContext context, String newName) {
        List<Either<TextDocumentEdit, ResourceOperation>> textDocumentEdits = new ArrayList<>();
        CompilerManager compilerManager = context.compilerManager();
        Position cursor = context.getCursorPosition();

        Optional<Path> projectRoot = context.compilerManager().getProjectRoot(context.getPath());
        List<Module> modules = context.compilerManager()
                .getModules(projectRoot.get());

        Document document = compilerManager
                .getDocument(context.getPath()).orElseThrow();
        LinePosition linePosition = LinePosition
                .from(cursor.getLine(), cursor.getCharacter());
        Optional<Symbol> symbol = compilerManager.getSemanticModel(context.getPath())
                .orElseThrow().symbol(document, linePosition);
        if (symbol.isEmpty()) {
            return textDocumentEdits;
        }

        Map<String, List<TextEdit>> textEditMap = new HashMap<>();
        
        for (Module module : modules) {
            SemanticModel semanticModel = module.getCompilation().getSemanticModel();
            String pkgName = context.compilerManager()
                    .getProject(context.getPath()).orElseThrow()
                    .currentPackage().packageName().value();
            String moduleName = module.moduleId().moduleName()
                    .replace(pkgName + ".", "");
            Path modulePath = projectRoot.get().resolve("modules")
                    .resolve(moduleName);
            List<Location> references = semanticModel.references(symbol.get());

            // Looping the reference and generate edits
            for (Location reference : references) {
                Range range = toRange(reference.lineRange());
                List<TextEdit> textEdits = new ArrayList<>();
                if (CommonUtils.isKeyword(newName)) {
                    /*
                    If the new name is a keyword we add annotated edits
                    for quoted and unquoted new names
                     */
                    String quotedName = "'" + newName;
                    TextEdit quoted = new AnnotatedTextEdit(range,
                            quotedName,
                            RenameChangeAnnotation.withQuote.getId());
                    TextEdit plain = new AnnotatedTextEdit(range,
                            newName,
                            RenameChangeAnnotation.withoutQuote.getId());
                    textEdits.add(quoted);
                    textEdits.add(plain);
                } else {
                    textEdits.add(new TextEdit(range, newName));
                }
                
                String uri = modulePath.resolve(reference.lineRange()
                        .filePath()).toUri().toString();
                if (textEditMap.containsKey(uri)) {
                    textEditMap.get(uri).addAll(textEdits);
                } else {
                    textEditMap.put(uri, textEdits);
                }
            }
        }
        
        textEditMap.forEach((uri, annotatedTextEdits) -> {
            TextDocumentEdit textDocumentEdit = new TextDocumentEdit();
            VersionedTextDocumentIdentifier identifier =
                    new VersionedTextDocumentIdentifier();
            identifier.setUri(uri);
            List<TextEdit> textEdits = new ArrayList<>(annotatedTextEdits);
            textDocumentEdit.setEdits(textEdits);
            textDocumentEdit.setTextDocument(identifier);
            
            textDocumentEdits.add(Either.forLeft(textDocumentEdit));
        });
        
        return textDocumentEdits;
    }

    public static PrepareRenameResult
    prepareRename(BalPrepareRenameContext context) {
        Token tokenAtCursor = context.getTokenAtCursor();
        if (tokenAtCursor.kind() != SyntaxKind.IDENTIFIER_TOKEN
                || CommonUtils.isKeyword(tokenAtCursor.text())) {
            return null;
        }

        LinePosition startLine = tokenAtCursor.lineRange().startLine();
        LinePosition endLine = tokenAtCursor.lineRange().endLine();

        PrepareRenameResult renameResult = new PrepareRenameResult();
        Range range = new Range();
        range.setStart(new Position(startLine.line(), startLine.offset()));
        range.setEnd(new Position(endLine.line(), endLine.offset()));
        renameResult.setPlaceholder("renamed_" + tokenAtCursor.text());
        renameResult.setRange(range);

        return renameResult;
    }

    private static Map<String, ChangeAnnotation> getChangeAnnotations() {
        Map<String, ChangeAnnotation> annotationMap = new HashMap<>();
        annotationMap.put(RenameChangeAnnotation.withQuote.getId(),
                RenameChangeAnnotation.withQuote.get());
        annotationMap.put(RenameChangeAnnotation.withoutQuote.getId(),
                RenameChangeAnnotation.withoutQuote.get());
        return annotationMap;
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

    enum RenameChangeAnnotation {
        withQuote("withQuote", "Quoted Rename",
                "Rename keyword with a quote"),
        withoutQuote("withoutQuote", "Un-quoted Rename",
                "Rename keyword without a quote");

        private final String id;
        private final String label;
        private final String description;

        RenameChangeAnnotation(String id, String label, String description) {
            this.id = id;
            this.label = label;
            this.description = description;
        }

        public ChangeAnnotation get() {
            ChangeAnnotation changeAnnotation = new ChangeAnnotation();
            changeAnnotation.setDescription(this.description);
            changeAnnotation.setLabel(this.label);
            changeAnnotation.setNeedsConfirmation(true);

            return changeAnnotation;
        }

        public String getId() {
            return id;
        }
    }
}
