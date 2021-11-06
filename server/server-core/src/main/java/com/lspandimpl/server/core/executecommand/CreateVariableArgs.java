package com.lspandimpl.server.core.executecommand;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public class CreateVariableArgs {
    private final Range range;
    private final String uri;
    private final String newText;

    public CreateVariableArgs(String newText, LineRange range, String uri, Diagnostic diagnostic) {
        this.newText = newText;
        LinePosition startLine = range.startLine();
        LinePosition endLine = range.endLine();
        
        this.range = new Range(new Position(startLine.line(), startLine.offset()),
                new Position(endLine.line(), endLine.offset()));
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public String getNewText() {
        return newText;
    }

    public Range getRange() {
        return range;
    }
}
