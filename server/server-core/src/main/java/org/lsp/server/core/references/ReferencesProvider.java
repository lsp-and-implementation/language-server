package org.lsp.server.core.references;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceContext;
import org.lsp.server.api.context.BalReferencesContext;
import org.lsp.server.ballerina.compiler.workspace.CompilerManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReferencesProvider {

    public static List<Location> references(BalReferencesContext context) {
        List<Location> locations = new ArrayList<>();
        Map<Module, List<io.ballerina.tools.diagnostics.Location>> references = findReferences(context);

        references
                .forEach((module, locationList) -> locationList
                        .forEach(location ->
                                locations.add(toLspLocation(module, location))));
        
        return locations;
    }

    private static Map<Module, List<io.ballerina.tools.diagnostics.Location>> findReferences(BalReferencesContext context) {
        Map<Module, List<io.ballerina.tools.diagnostics.Location>> moduleLocationMap = new HashMap<>();
        boolean includeDeclaration = context.getReferenceContext().isIncludeDeclaration();
        Path path = context.getPath();
        CompilerManager compilerManager = context.compilerManager();
        Project project = compilerManager.getProject(path).orElseThrow();
        Document document = compilerManager.getDocument(path)
                .orElseThrow();
        Position cursorPos = context.getCursorPosition();
        LinePosition linePos = LinePosition.from(cursorPos.getLine(),
                cursorPos.getCharacter());
        Symbol symbolAtCursor = compilerManager.getSemanticModel(path)
                .orElseThrow().symbol(document, linePos)
                .orElseThrow();
        project.currentPackage().moduleIds().forEach(moduleId -> {
            SemanticModel semanticModel = project.currentPackage()
                    .getCompilation().getSemanticModel(moduleId);
            List<io.ballerina.tools.diagnostics.Location> references =
                    semanticModel.references(symbolAtCursor, includeDeclaration);
            if (references.isEmpty()) {
                return;
            }
            Module module = project.currentPackage().module(moduleId);
            moduleLocationMap.put(module, references);
        });

        return moduleLocationMap;
    }

    private static Location toLspLocation(Module module, io.ballerina.tools.diagnostics.Location diagLocation) {
        Location location = new Location();

        String filePath = diagLocation.lineRange().filePath();
        String uri;
        if (module.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            uri = module.project().sourceRoot().toUri().toString();
        } else if (module.isDefaultModule()) {
            uri = module.project().sourceRoot().resolve(filePath).toUri().toString();
        } else {
            uri = module.project().sourceRoot()
                    .resolve("modules")
                    .resolve(module.moduleName().moduleNamePart())
                    .resolve(filePath).toUri().toString();
        }

        Range range = toRange(diagLocation.lineRange());
        location.setUri(uri);
        location.setRange(range);
        
        return location;
    }

    public static String toUri(Module module, io.ballerina.tools.diagnostics.Location location) {
        String filePath = location.lineRange().filePath();

        if (module.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            return module.project().sourceRoot().toUri().toString();
        } else if (module.isDefaultModule()) {
            return module.project().sourceRoot().resolve(filePath).toUri().toString();
        } else {
            return module.project().sourceRoot()
                    .resolve("modules")
                    .resolve(module.moduleName().moduleNamePart())
                    .resolve(filePath).toUri().toString();
        }
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
