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
import org.lsp.server.api.context.BalPosBasedContext;
import org.lsp.server.api.context.BalReferencesContext;
import org.lsp.server.ballerina.compiler.workspace.CompilerManager;
import org.lsp.server.core.AbstractProvider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ReferencesProvider extends AbstractProvider {

    public static List<Location> references(BalReferencesContext context) {
        List<Location> locations = new ArrayList<>();
        boolean includeDeclaration =
                context.getReferenceContext().isIncludeDeclaration();
        // Capture the references from Ballerina Compiler APIs
        Map<Module, List<io.ballerina.tools.diagnostics.Location>> references
                = findReferences(context, includeDeclaration);

        // Generate the references response
        references
                .forEach((module, locationList) -> locationList
                        .forEach(location ->
                                locations.add(toLspLocation(module, location))));
        
        return locations;
    }
    
    private static Map<Module, List<io.ballerina.tools.diagnostics.Location>>
    findReferences(BalPosBasedContext context, boolean includeDeclaration) {
        Map<Module, List<io.ballerina.tools.diagnostics.Location>>
                moduleLocationMap = new HashMap<>();
        Path path = context.getPath();
        CompilerManager compilerManager = context.compilerManager();
        Project project = compilerManager.getProject(path).orElseThrow();
        Document document = compilerManager.getDocument(path)
                .orElseThrow();
        Position cursorPos = context.getCursorPosition();
        LinePosition linePos = LinePosition.from(cursorPos.getLine(),
                cursorPos.getCharacter());
        // Get the symbol at the cursor
        Symbol symbolAtCursor = compilerManager.getSemanticModel(path)
                .orElseThrow().symbol(document, linePos)
                .orElseThrow();
        // Iterate over each of the modules and find the references
        // of the symbol at the cursor position
        project.currentPackage().moduleIds().forEach(moduleId -> {
            SemanticModel semanticModel = project.currentPackage()
                    .getCompilation().getSemanticModel(moduleId);
            List<io.ballerina.tools.diagnostics.Location>
                    references = semanticModel
                    .references(symbolAtCursor, includeDeclaration);
            if (references.isEmpty()) {
                return;
            }
            Module module = project.currentPackage().module(moduleId);
            moduleLocationMap.put(module, references);
        });

        return moduleLocationMap;
    }
}
