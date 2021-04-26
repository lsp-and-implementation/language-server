package org.lsp.server.core.references;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.lsp.server.api.context.BalReferencesContext;
import org.lsp.server.ballerina.compiler.workspace.CompilerManager;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ReferencesProvider {
    public static List<Location> references(BalReferencesContext context) {
        Path path = context.getPath();
        CompilerManager compilerManager = context.compilerManager();
        SemanticModel semanticModel = compilerManager
                .getSemanticModel(path).orElseThrow();
        Document document = compilerManager.getDocument(path)
                .orElseThrow();
        Position cursorPos = context.getCursorPosition();
        LinePosition linePos = LinePosition.from(cursorPos.getLine(),
                cursorPos.getCharacter());
        List<io.ballerina.tools.diagnostics.Location> references =
                semanticModel.references(document, linePos);
        ReferenceContext referenceContext =
                context.getReferenceContext();
        if (!referenceContext.isIncludeDeclaration()) {
            /*
            Filter the references except the declaration instance
             */
//            ...
        }
        return references.stream()
                .map(ReferencesProvider::toLspLocation)
                .collect(Collectors.toList());
    }

    private static Location toLspLocation(io.ballerina.tools.diagnostics.Location location) {
        return null;
    }
}
