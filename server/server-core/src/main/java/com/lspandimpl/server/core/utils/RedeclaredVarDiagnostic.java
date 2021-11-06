package com.lspandimpl.server.core.utils;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticProperty;
import io.ballerina.tools.diagnostics.DiagnosticRelatedInformation;
import io.ballerina.tools.diagnostics.Location;

import java.util.Collections;
import java.util.List;

public class RedeclaredVarDiagnostic extends Diagnostic {

    private final DiagnosticInfo diagnosticInfo;
    private final Location location;
    private final String message;
    private final DiagnosticRelatedInformation relatedInformation;

    public RedeclaredVarDiagnostic(DiagnosticInfo diagnosticInfo, Location location, String message,
                                   DiagnosticRelatedInformation relatedInformation) {
        this.diagnosticInfo = diagnosticInfo;
        this.location = location;
        this.message = message;
        this.relatedInformation = relatedInformation;
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

    public DiagnosticRelatedInformation relatedInformation() {
        return relatedInformation;
    }
}
