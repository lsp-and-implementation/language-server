package com.lspandimpl.server.core.utils;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeLocation;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import com.lspandimpl.server.api.context.BaseOperationContext;
import com.lspandimpl.server.ballerina.compiler.workspace.CompilerManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BallerinaLinter {
    public static List<Diagnostic> getFunctionDiagnostics(Path path, BaseOperationContext context) {
        CompilerManager compilerManager = context.compilerManager();
        Optional<Project> project = compilerManager.getProject(path);
        if (project.isEmpty()) {
            return Collections.emptyList();
        }
        Optional<SyntaxTree> syntaxTree = compilerManager.getSyntaxTree(path);
        if (syntaxTree.isEmpty() || !syntaxTree.get().containsModulePart()) {
            return Collections.emptyList();
        }
        List<Diagnostic> diagnostics = new ArrayList<>();
        ((ModulePartNode) syntaxTree.get().rootNode()).members().forEach(member -> {
            if (member.kind() != SyntaxKind.FUNCTION_DEFINITION) {
                return;
            }
            IdentifierToken funcName = ((FunctionDefinitionNode) member).functionName();
            Optional<SemanticModel> semanticModel = compilerManager.getSemanticModel(path);
            if (semanticModel.isEmpty()) {
                return;
            }
            Optional<Symbol> symbol = semanticModel.get().symbol(funcName);
            symbol.ifPresent(fSymbol -> {
                Path sourceRoot = context.compilerManager().getProject(path).orElseThrow().sourceRoot();
                String relativizedPath = sourceRoot.relativize(path).toString();
                if (fSymbol.kind() == SymbolKind.FUNCTION && isDeprecatedFunction(fSymbol)) {
                    diagnostics.add(getDeprecatedFunctionDiagnostic((FunctionDefinitionNode) member, relativizedPath));
                }
                List<Location> references = semanticModel.get().references(fSymbol);
                if (references.size() == 1) {
                    diagnostics.add(getUnusedFunctionDiagnostic((FunctionDefinitionNode) member, relativizedPath));
                }
            });
        });

        return diagnostics;
    }

    private static boolean isDeprecatedFunction(Symbol fSymbol) {
        if (fSymbol.kind() != SymbolKind.FUNCTION) {
            return false;
        }
        List<AnnotationSymbol> annotations = ((FunctionSymbol) fSymbol).annotations();
        for (AnnotationSymbol annotation : annotations) {
            if (annotation.getName().isPresent() && annotation.getName().get().equals("deprecated")) {
                return true;
            }
        }

        return false;
    }

    private static Diagnostic getUnusedFunctionDiagnostic(FunctionDefinitionNode node, String path) {
        String message = "Unused Function: " + node.functionName().text();
        NodeLocation location = node.functionName().location();
        LinterDiagnosticCodes code = LinterDiagnosticCodes.LINTER001;
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(code.getDiagnosticCode(), code.getMessage(),
                DiagnosticSeverity.WARNING);
        return new LinterDiagnostic(diagnosticInfo, location, message, path);
    }

    private static Diagnostic getDeprecatedFunctionDiagnostic(FunctionDefinitionNode node, String path) {
        String message = "Deprecated Function: " + node.functionName().text();
        NodeLocation location = node.functionName().location();
        LinterDiagnosticCodes code = LinterDiagnosticCodes.LINTER002;
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(code.getDiagnosticCode(), code.getMessage(),
                DiagnosticSeverity.WARNING);
        return new LinterDiagnostic(diagnosticInfo, location, message, path);
    }

    public static List<RedeclaredVarDiagnostic> getRedeclaredVarDiagnostics(Path path, BaseOperationContext context) {
        return Collections.emptyList();
    }

    private String getModuleLocation(Symbol symbol, Project project) {
        String moduleName = symbol.getModule().orElseThrow().getName().orElseThrow();
        PackageName packageName = project.currentPackage().getDefaultModule().moduleName().packageName();
        return "modules" + "/" + moduleName.replace(packageName.value() + ".", "");
    }
}
