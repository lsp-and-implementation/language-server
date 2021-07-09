package org.lsp.server.core.codeaction;

import io.ballerina.compiler.api.SemanticModel;
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
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.lsp.server.api.context.BalCodeActionContext;
import org.lsp.server.core.executecommand.CreateVariableArgs;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class CodeActionProvider {
    private static final String VAR_ASSIGNMENT_REQUIRED = "variable assignment is required";
    
    public static List<Either<Command, CodeAction>>
    getCodeAction(BalCodeActionContext context, CodeActionParams params) {
        List<Either<Command, CodeAction>> codeActions = new ArrayList<>();
        List<Diagnostic> diags = getDiagnostics(context, params.getRange());
        Optional<Node> topLevelNode = getTopLevelNode(context, params.getRange());
        List<String> diagMessages = diags.stream()
                .map(diag -> diag.message().toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());
        if (diagMessages.contains(VAR_ASSIGNMENT_REQUIRED)) {
            Diagnostic diagnostic = diags.get(diagMessages.indexOf(VAR_ASSIGNMENT_REQUIRED));
            Command createVarCommand = getCreateVarCommand(context, diagnostic, params.getRange());
            Either<Command, CodeAction> command = Either.forLeft(createVarCommand);
            return Collections.singletonList(command);
        }
        if (topLevelNode.isPresent() && topLevelNode.get().kind()
                == SyntaxKind.FUNCTION_DEFINITION && documentationEnabled(context)) {
//            CodeAction codeAction = getAddDocsCodeAction.get(context, params);
//                codeActions.add(Either.forRight(codeAction));
        }

        return codeActions;
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
            if (mStart.line() >= diagStart.getLine()
                    && mEnd.line() <= diagEnd.getLine()) {
                return Optional.of(member);
            }
        }
        
        return Optional.empty();
    }
    
    private static Command
    getCreateVarCommand(BalCodeActionContext context,
                        Diagnostic diagnostic,
                        Range range) {
        String expr = context.getNodeAtCursor().toSourceCode();
        Command command = new Command();
        command.setCommand(BalCommand.CREATE_VAR.getCommand());
        command.setTitle(BalCommand.CREATE_VAR.getTitle());
        List<Object> args = new ArrayList<>();
        String typeDescriptor = getExpectedTypeDescriptor(range);
        String uri = context.getPath().toUri().toString();
        String newText = typeDescriptor + " = " + expr;
        CreateVariableArgs createVarArgs =
                new CreateVariableArgs(newText, range, uri, diagnostic);
        args.add(new CommandArgument("params", createVarArgs));
        command.setArguments(args);

        return command;
    }

    private static WorkspaceEdit getWorkspaceEdit(BalCodeActionContext context, Range range, CodeActionParams params) {
        return null;
    }

    private static org.eclipse.lsp4j.Diagnostic toDiagnostic(Diagnostic diagnostic) {
        return null;
    }

    private static String getExpectedTypeDescriptor(Range range) {
        return null;
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

        return (Boolean) (configValue.get(0));
    }
}
