package com.lspandimpl.server.core.utils;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.Location;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class LinterDiagnostic extends Diagnostic {

    private final DiagnosticInfo diagnosticInfo;
    private final Location location;
    private final String message;
    private final String uri;

    public LinterDiagnostic(DiagnosticInfo diagnosticInfo, Location location, String message, String uri) {
        this.diagnosticInfo = diagnosticInfo;
        this.location = location;
        this.message = message;
        this.uri = uri;
    }

    @Override
    public Location location() {
        return this.location;
    }

    @Override
    public DiagnosticInfo diagnosticInfo() {
        return this.diagnosticInfo;
    }

    @Override
    public String message() {
        return this.message;
    }

    @Override
    public List<DiagnosticProperty<?>> properties() {
        return Collections.emptyList();
    }

    public String getUri() {
        return uri;
    }
}
