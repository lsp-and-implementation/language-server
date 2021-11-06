package com.lspandimpl.server.core.codeaction;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lspandimpl.server.core.utils.CommonUtils;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import com.lspandimpl.server.api.ConfigurationHolder;
import com.lspandimpl.server.api.context.BalCodeActionContext;
import com.lspandimpl.server.api.context.BalTextDocumentContext;
import com.lspandimpl.server.core.executecommand.CreateVariableArgs;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class CodeActionProvider {
    private static final String VAR_ASSIGNMENT_REQUIRED = "variable assignment is required";
    private static final String UNDEFINED_FUNCTION = "undefined function";
    private static final Gson GSON = new Gson();

    public static List<Either<Command, CodeAction>>
    getCodeAction(BalCodeActionContext context, CodeActionParams params) {
        List<Either<Command, CodeAction>> codeActions = new ArrayList<>();
        List<Diagnostic> diags = getDiagnostics(context, params.getRange());
        Optional<Node> topLevelNode =
                getTopLevelNode(context, params.getRange());
        List<String> diagMessages = diags.stream()
                .map(diag -> diag.message().toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
        if (diagMessages.contains(VAR_ASSIGNMENT_REQUIRED)) {
            Diagnostic diagnostic = diags.get(diagMessages
                    .indexOf(VAR_ASSIGNMENT_REQUIRED));
            Command createVarCommand =
                    getCreateVarCommand(context, diagnostic,
                            params.getRange());
            Either<Command, CodeAction> command =
                    Either.forLeft(createVarCommand);
            return Collections.singletonList(command);

            // Enable the following for the code action based implementation.
            // Before that comment the lines 69-74
            // CodeAction createVarCodeAction
            //         = getCreateVarCodeAction(context, params.getRange(), diagnostic);
            // Either<Command, CodeAction> codeAction =
            //         Either.forRight(createVarCodeAction);
            // return Collections.singletonList(codeAction);
        }
        codeActions.add(getOrganizeImportsCodeAction(params));
        ConfigurationHolder configHolder = context.clientConfigHolder();
        if (topLevelNode.isPresent() && topLevelNode.get().kind()
                == SyntaxKind.FUNCTION_DEFINITION
                && configHolder.isDocumentationCodeActionEnabled()) {
            codeActions.add(getAddDocsCodeAction(context,
                    params,
                    topLevelNode.get())
            );
        }
        /*
        Comment out the lines 83-89 and use the bellow variation to enable the workspace/configuration
        request based code action
         */
//        if (topLevelNode.isPresent() && topLevelNode.get().kind()
//                == SyntaxKind.FUNCTION_DEFINITION
//                && documentationEnabled(context)) {
//            codeActions.add(getAddDocsCodeAction(context,
//                    params,
//                    topLevelNode.get())
//            );
//        }

        // Checks for the create function code action and route to the relevant provider
        Optional<Diagnostic> undefinedFunctionDiagnostic = getUndefinedFunctionDiagnostic(diags);

        if (undefinedFunctionDiagnostic.isPresent()) {
            Diagnostic diagnostic = undefinedFunctionDiagnostic.get();
            codeActions.add(Either.forRight(CreateFunctionCodeAction.getCodeAction(context, diagnostic, params)));
        }

        return codeActions;
    }

    public static CodeAction
    resolve(BalTextDocumentContext context, CodeAction unresolved) {
        if (unresolved.getTitle().equals(BalCommand.ORGANIZE_IMPORTS.getTitle())) {
            WorkspaceEdit workspaceEdit =
                    getOrganizeImportEdit(context, context.getPath());
            unresolved.setEdit(workspaceEdit);

            return unresolved;
        }
        if (unresolved.getTitle().equals(BalCommand.CREATE_FUNCTION.getTitle())) {
            return CreateFunctionCodeActionResolve.getResolved(context, unresolved);
        }

        return null;
    }

    private static Optional<Node> getTopLevelNode(BalCodeActionContext context,
                                                  Range range) {
        Optional<SyntaxTree> syntaxTree = context.currentSyntaxTree();
        if (syntaxTree.isEmpty()) {
            return Optional.empty();
        }
        Position diagStart = range.getStart();
        Position diagEnd = range.getEnd();
        NodeList<ModuleMemberDeclarationNode> members =
                ((ModulePartNode) syntaxTree.get().rootNode()).members();
        for (ModuleMemberDeclarationNode member : members) {
            LineRange lineRange = member.lineRange();
            LinePosition mStart = lineRange.startLine();
            LinePosition mEnd = lineRange.endLine();
            if (mStart.line() <= diagStart.getLine()
                    && mEnd.line() >= diagEnd.getLine()) {
                return Optional.of(member);
            }
        }

        return Optional.empty();
    }

    private static Either<Command, CodeAction>
    getOrganizeImportsCodeAction(CodeActionParams params) {
        CodeAction codeAction = new CodeAction();
        JsonObject data = new JsonObject();
        data.addProperty("uri", params.getTextDocument().getUri());
        codeAction.setKind(CodeActionKind.SourceOrganizeImports);
        codeAction.setIsPreferred(true);
        codeAction.setTitle(BalCommand.ORGANIZE_IMPORTS.getTitle());
        codeAction.setData(data);

        return Either.forRight(codeAction);
    }

    private static Either<Command, CodeAction>
    getAddDocsCodeAction(BalCodeActionContext context, CodeActionParams params, Node node) {
        CodeAction codeAction = new CodeAction();
        JsonObject data = new JsonObject();
        data.addProperty("uri", params.getTextDocument().getUri());
        codeAction.setIsPreferred(true);
        codeAction.setTitle(BalCommand.ADD_DOC.getTitle());
        codeAction.setData(data);

        return Either.forRight(codeAction);
    }

    private static Command
    getCreateVarCommand(BalCodeActionContext context,
                        Diagnostic diagnostic,
                        Range range) {
        String expr = context.getNodeAtCursor().toSourceCode().trim();
        Command command = new Command();
        command.setCommand(BalCommand.CREATE_VAR.getCommand());
        command.setTitle(BalCommand.CREATE_VAR.getTitle());
        List<Object> args = new ArrayList<>();
        String typeDescriptor = CommonUtils.getExpectedTypeDescriptor(range, context);
        String uri = context.getPath().toUri().toString();
        String newText = typeDescriptor + " varName = " + expr;
        LineRange lineRange = context.getNodeAtCursor().lineRange();
        CreateVariableArgs createVarArgs =
                new CreateVariableArgs(newText, lineRange, uri, diagnostic);
        args.add(new CommandArgument("params", createVarArgs));
        command.setArguments(args);

        return command;
    }

    private static CodeAction
    getCreateVarCodeAction(BalCodeActionContext context,
                           Range range,
                           Diagnostic diagnostic) {
        CodeAction codeAction = new CodeAction();
        codeAction.setTitle(BalCommand.CREATE_VAR.getTitle());
        codeAction.setKind(CodeActionKind.QuickFix);
        /*
        Setting the diagnostic will show a quickfix link when hover over
        the diagnostic
         */
        codeAction.setDiagnostics(Collections
                .singletonList(CommonUtils.toDiagnostic(diagnostic)));
        codeAction.setEdit(getCreateVarWorkspaceEdit(context, range));

        return codeAction;
    }

    private static WorkspaceEdit getCreateVarWorkspaceEdit(BalCodeActionContext context,
                                                           Range range) {
        WorkspaceEdit workspaceEdit = new WorkspaceEdit();
        Map<String, List<TextEdit>> textEditMap = new HashMap<>();
        TextEdit textEdit = new TextEdit();

        String expr = context.getNodeAtCursor().toSourceCode().trim();
        String typeDescriptor = CommonUtils.getExpectedTypeDescriptor(range, context);
        String uri = context.getPath().toUri().toString();
        String newText = typeDescriptor + " varName = " + expr;

        textEdit.setRange(toRange(context.getNodeAtCursor().lineRange()));
        textEdit.setNewText(newText);
        textEditMap.put(uri, Collections.singletonList(textEdit));

        workspaceEdit.setChanges(textEditMap);

        return workspaceEdit;
    }

    private static WorkspaceEdit
    getOrganizeImportEdit(BalTextDocumentContext context,
                          Path path) {
        SyntaxTree syntaxTree =
                context.compilerManager()
                        .getSyntaxTree(path).orElseThrow();
        NodeList<ImportDeclarationNode> imports =
                ((ModulePartNode) syntaxTree.rootNode()).imports();

        if (imports.size() < 1) {
            return null;
        }
        ImportDeclarationNode firstImport = imports.get(0);
        ImportDeclarationNode lastImport =
                imports.get(imports.size() - 1);

        String reOrdered = imports.stream()
                .map(importNode -> importNode.toSourceCode().trim())
                .sorted()
                .collect(Collectors.joining(CommonUtils.LINE_SEPARATOR));

        WorkspaceEdit workspaceEdit = new WorkspaceEdit();
        TextEdit textEdit = new TextEdit();

        LinePosition startLine = firstImport.lineRange().startLine();
        LinePosition endLine = lastImport.lineRange().endLine();
        Range range = toRange(LineRange.from(path.toString(), startLine, endLine));
        textEdit.setRange(range);
        textEdit.setNewText(reOrdered);
        Map<String, List<TextEdit>> textEditMap = new HashMap<>();
        textEditMap.put(path.toUri().toString(), Collections.singletonList(textEdit));

        workspaceEdit.setChanges(textEditMap);
        return workspaceEdit;
    }

    private static List<Diagnostic> getDiagnostics(BalCodeActionContext context, Range range) {
        Path path = context.getPath();
        Optional<SemanticModel> semanticModel = context.compilerManager().getSemanticModel(path);
        if (semanticModel.isEmpty()) {
            return Collections.emptyList();
        }
        Position start = range.getStart();
        Position end = range.getEnd();
        return semanticModel.get().diagnostics().stream()
                .filter(diagnostic -> {
                    LineRange diagRange = diagnostic.location().lineRange();
                    LinePosition dStart = diagRange.startLine();
                    LinePosition dEnd = diagRange.endLine();

                    return dStart.line() >= start.getLine() && dEnd.line() <= end.getLine();
                }).collect(Collectors.toList());
    }

    /**
     * Get the configuration for ballerina.codeAction.documentation config option.
     * Can be used alongside the
     *
     * @param context code action context
     * @return {@link Boolean}
     */
    private static boolean documentationEnabled(BalCodeActionContext context) {
        LanguageClient client = context.getClient();
        ConfigurationParams params = new ConfigurationParams();
        ConfigurationItem item = new ConfigurationItem();
        item.setSection("ballerina.codeAction.documentation");
        params.setItems(Collections.singletonList(item));
        CompletableFuture<List<Object>> configuration =
                client.configuration(params);
        List<Object> configValue;
        try {
            configValue = configuration.get();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }

        return GSON.toJsonTree(configValue.get(0)).getAsBoolean();
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

    private static Optional<Diagnostic> getUndefinedFunctionDiagnostic(List<Diagnostic> diagnostics) {
        return diagnostics.stream()
                .filter(diagnostic -> diagnostic.message().contains(UNDEFINED_FUNCTION))
                .findFirst();
    }
}
