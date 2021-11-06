package com.lspandimpl.server.core.utils;

public enum LinterDiagnosticCodes {
    LINTER001("LINTER001", "Unused Function"),
    LINTER002("LINTER002", "Deprecated Function");

    private final String diagnosticCode;
    private final String message;
    LinterDiagnosticCodes(String diagnosticCode, String message) {
        this.diagnosticCode = diagnosticCode;
        this.message = message;
    }

    public String getDiagnosticCode() {
        return diagnosticCode;
    }

    public String getMessage() {
        return message;
    }
}
