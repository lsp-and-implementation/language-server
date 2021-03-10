/*
 * Copyright (c) 2021, Nadeeshaan Gunasinghe, Nipuna Marcus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lsp.server.core;

import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Capture and handle the diagnostics in the respective language server instance.
 *
 * @since 1.0.0
 */
public class DiagnosticsPublisherImpl implements org.lsp.server.api.DiagnosticsPublisher {
    private final LanguageClient client;
    private Map<String, List<Diagnostic>> previousDiagnostics = new ConcurrentHashMap<>();

    public DiagnosticsPublisherImpl(LanguageClient client) {
        this.client = client;
    }

    @Override
    public void publish(Project project) {
        DiagnosticResult diagnosticResult = project.currentPackage().getCompilation().diagnosticResult();
        Map<String, List<Diagnostic>> diagnostics = new HashMap<>();     
        diagnosticResult.diagnostics().forEach(diagnostic -> {
            String path = diagnostic.location().lineRange().filePath();
            Diagnostic computedDiag = this.getDiagnostic(diagnostic);
            if (diagnostics.containsKey(path)) {
                diagnostics.get(path).add(computedDiag);
            } else {
                List<Diagnostic> diags = new ArrayList<>();
                diags.add(computedDiag);
                diagnostics.put(path, diags);
            }
        });
        
        this.previousDiagnostics.forEach((path, diagnosticList) -> {
            if (!diagnostics.containsKey(path)) {
                diagnostics.put(path, new ArrayList<>());
            }
        });
        diagnostics.forEach((path, diagList) -> {
            PublishDiagnosticsParams params = new PublishDiagnosticsParams();
            params.setDiagnostics(diagList);
            params.setUri(path);
            
            this.client.publishDiagnostics(params);
        });
        
        this.previousDiagnostics = diagnostics;
    }
    
    private Diagnostic getDiagnostic(io.ballerina.tools.diagnostics.Diagnostic diagnostic) {
        DiagnosticInfo diagnosticInfo = diagnostic.diagnosticInfo();
        Diagnostic diag = new Diagnostic();
        diag.setMessage(diagnostic.message());
        diag.setCode(diagnosticInfo.code());

        LineRange lineRange = diagnostic.location().lineRange();
        Position start = new Position(lineRange.startLine().line(), lineRange.startLine().offset());
        Position end = new Position(lineRange.endLine().line(), lineRange.endLine().offset());
        diag.setRange(new Range(start, end));
        
        switch (diagnosticInfo.severity()) {
            case ERROR:
                diag.setSeverity(DiagnosticSeverity.Error);
                break;
            case HINT:
                diag.setSeverity(DiagnosticSeverity.Hint);
                break;
            case INFO:
                diag.setSeverity(DiagnosticSeverity.Information);
                break;
            case WARNING:
                diag.setSeverity(DiagnosticSeverity.Warning);
                break;
            default:
                break;
        }
        
        return diag;
    }
}
