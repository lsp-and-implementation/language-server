package org.lsp.server.core.executecommand;

import org.eclipse.lsp4j.Range;

public class CreateVariableArgs {
    private Range range;
    private String uri;
    private String newText;

    public CreateVariableArgs(String newText, Range range, String uri) {
        this.newText = newText;
        this.range = range;
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
