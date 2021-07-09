package org.lsp.server.core.executecommand;

import io.ballerina.tools.diagnostics.Diagnostic;
import org.eclipse.lsp4j.Range;

public class CreateVariableArgs {
    private Range range;
    private String uri;
    private String newText;
    private Diagnostic diagnostic;

    public CreateVariableArgs(String newText, Range range, String uri, Diagnostic diagnostic) {
        this.newText = newText;
        this.range = range;
        this.uri = uri;
        this.diagnostic = diagnostic;
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

    public Diagnostic getDiagnostic() {
        return diagnostic;
    }
}
