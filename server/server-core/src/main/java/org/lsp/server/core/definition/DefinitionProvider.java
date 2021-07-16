package org.lsp.server.core.definition;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.lsp.server.api.context.BalDefinitionContext;
import org.lsp.server.api.context.BalGotoImplContext;
import org.lsp.server.api.context.BalPosBasedContext;
import org.lsp.server.api.context.BalTypeDefContext;
import org.lsp.server.ballerina.compiler.workspace.CompilerManager;
import org.lsp.server.core.AbstractProvider;
import org.lsp.server.core.utils.CommonUtils;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DefinitionProvider extends AbstractProvider {
    public static List<Location> definition(BalDefinitionContext context) {
        // In this implementation we only consider the definition
        // of the constructs in the same project.
        Path path = context.getPath();
        CompilerManager compilerManager = context.compilerManager();
        SemanticModel semanticModel = compilerManager
                .getSemanticModel(path).orElseThrow();
        Document document = compilerManager.getDocument(path)
                .orElseThrow();
        Position cursorPos = context.getCursorPosition();
        LinePosition linePos = LinePosition.from(cursorPos.getLine(),
                cursorPos.getCharacter());
        Optional<Symbol> symbol =
                semanticModel.symbol(document, linePos);
        if (symbol.isEmpty()) {
            return Collections.emptyList();
        }
        io.ballerina.tools.diagnostics.Location location = symbol.get().getLocation().orElseThrow();

        return Collections.singletonList(toLspLocation(context, symbol.get(), location));
    }

    private static Location toLspLocation(BalPosBasedContext context, Symbol symbol,
                                          io.ballerina.tools.diagnostics.Location location) {
        Optional<Path> projectRoot = context.compilerManager().getProjectRoot(context.getPath());
        Optional<Project> project = context.compilerManager().getProject(context.getPath());
        String moduleName = symbol.getModule().orElseThrow().id().moduleName();

        if (project.isEmpty() || projectRoot.isEmpty()) {
            throw new RuntimeException("Cannot find valid project");
        }

        String packageName = project.get().currentPackage().packageName().value();
        moduleName = moduleName.replace(packageName + ".", "");
        Path definitionPath = projectRoot.get().resolve("modules").resolve(moduleName)
                .resolve(location.lineRange().filePath());
        String uri = definitionPath.toUri().toString();

        Location defLocation = new Location();
        Range range = toRange(location.lineRange());
        defLocation.setRange(range);
        defLocation.setUri(uri);

        return defLocation;
    }

    public static List<Location>
    typeDefinition(BalTypeDefContext context) {
        Path path = context.getPath();
        CompilerManager compilerManager = context.compilerManager();
        SemanticModel semanticModel = compilerManager
                .getSemanticModel(path).orElseThrow();
        Document document = compilerManager.getDocument(path)
                .orElseThrow();
        Position cursorPos = context.getCursorPosition();
        LinePosition linePos = LinePosition.from(cursorPos.getLine(),
                cursorPos.getCharacter());
        Optional<Symbol> symbol =
                semanticModel.symbol(document, linePos);
        if (symbol.isEmpty()) {
            return Collections.emptyList();
        }
        /*
        Capture the type symbol of the given symbol
         */
        TypeSymbol typeSymbol =
                CommonUtils.getTypeDefinition(symbol.get()).orElseThrow();
        io.ballerina.tools.diagnostics.Location location =
                typeSymbol.getLocation().orElseThrow();

        return Collections.singletonList(toLspLocation(context, symbol.get(), location));
    }

    public static List<Location>
    implementation(BalGotoImplContext context) {
        Path path = context.getPath();
        CompilerManager compilerManager = context.compilerManager();
        SemanticModel semanticModel = compilerManager
                .getSemanticModel(path).orElseThrow();
        Document document = compilerManager.getDocument(path)
                .orElseThrow();
        Position cursorPos = context.getCursorPosition();
        LinePosition linePos = LinePosition.from(cursorPos.getLine(),
                cursorPos.getCharacter());
        Optional<Symbol> symbol =
                semanticModel.symbol(document, linePos);
        /*
        Implementation is only allowed for method symbols.
         */
        if (symbol.isEmpty()
                || symbol.get().kind() != SymbolKind.METHOD) {
            return Collections.emptyList();
        }
        io.ballerina.tools.diagnostics.Location location =
                symbol.get().getLocation().orElseThrow();

        return Collections.singletonList(toLspLocation(context, symbol.get(), location));
    }
}
