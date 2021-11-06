package com.lspandimpl.server.core;

import io.ballerina.projects.Module;
import io.ballerina.projects.ProjectKind;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public abstract class AbstractProvider {
    
    protected static Location toLspLocation(Module module, io.ballerina.tools.diagnostics.Location diagLocation) {
        Location location = new Location();
        String uri = toUri(module, diagLocation);

        Range range = toRange(diagLocation.lineRange());
        location.setUri(uri);
        location.setRange(range);

        return location;
    }

    protected static String toUri(Module module, io.ballerina.tools.diagnostics.Location location) {
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

    public static Range toRange(LineRange lineRange) {
        Range range = new Range();
        range.setStart(toPosition(lineRange.startLine()));
        range.setEnd(toPosition(lineRange.endLine()));

        return range;
    }
}
