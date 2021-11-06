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
package com.lspandimpl.server.core;

import com.lspandimpl.server.core.utils.BallerinaLinter;
import com.lspandimpl.server.core.utils.LinterDiagnostic;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticRelatedInformation;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DiagnosticTag;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.services.LanguageClient;
import com.lspandimpl.server.api.DiagnosticsPublisher;
import com.lspandimpl.server.api.context.BaseOperationContext;
import com.lspandimpl.server.api.context.LSContext;
import com.lspandimpl.server.core.utils.LinterDiagnosticCodes;
import com.lspandimpl.server.core.utils.RedeclaredVarDiagnostic;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Capture and handle the diagnostics in the respective language server instance.
 *
 * @since 1.0.0
 */
public class DiagnosticsPublisherImpl implements DiagnosticsPublisher {
    private final LanguageClient client;
    private Map<String, List<Diagnostic>> previousDiagnostics = new ConcurrentHashMap<>();
    private static final LSContext.Key<DiagnosticsPublisher> DIAGNOSTICS_PUBLISHER_KEY = new LSContext.Key<>();

    public static DiagnosticsPublisher getInstance(LSContext serverContext) {
        DiagnosticsPublisher diagnosticsPublisher = serverContext.get(DIAGNOSTICS_PUBLISHER_KEY);
        if (diagnosticsPublisher == null) {
            diagnosticsPublisher = new DiagnosticsPublisherImpl(serverContext);
        }

        return diagnosticsPublisher;
    }

    private DiagnosticsPublisherImpl(LSContext serverContext) {
        serverContext.put(DIAGNOSTICS_PUBLISHER_KEY, this);
        this.client = serverContext.getClient();
    }

    @Override
    public void publish(BaseOperationContext context, Path path) {
        Optional<Project> project =
                context.compilerManager().getProject(path);
        if (project.isEmpty()) {
            return;
        }
        DiagnosticResult diagResult = project.get().currentPackage()
                .getCompilation().diagnosticResult();
        Map<String, List<Diagnostic>> diagnostics = new HashMap<>();
        // Get the compiler generated diagnostics
        List<io.ballerina.tools.diagnostics.Diagnostic>
                allDiagnostics = new ArrayList<>(diagResult.diagnostics());
        // Get the diagnostics from the linter
        allDiagnostics.addAll(BallerinaLinter
                .getFunctionDiagnostics(path, context));
        allDiagnostics.addAll(BallerinaLinter
                .getRedeclaredVarDiagnostics(path, context));
        // Fill the diagnostics to the return list
        allDiagnostics.forEach(diagnostic -> {
            String diagPath;
            if (diagnostic instanceof LinterDiagnostic) {
                diagPath = ((LinterDiagnostic) diagnostic).getUri();
            } else {
                diagPath = diagnostic.location().lineRange().filePath();
            }
            Diagnostic computedDiag = this.getDiagnostic(diagnostic);
            List<DiagnosticTag> tags = new ArrayList<>();
            if (diagnostic.diagnosticInfo().code().equals(LinterDiagnosticCodes.LINTER001.getDiagnosticCode())) {
                tags.add(DiagnosticTag.Unnecessary);
            }
            if (diagnostic.diagnosticInfo().code().equals(LinterDiagnosticCodes.LINTER002.getDiagnosticCode())) {
                tags.add(DiagnosticTag.Deprecated);
            }
            computedDiag.setTags(tags);
            if (diagnostic instanceof RedeclaredVarDiagnostic) {
                io.ballerina.tools.diagnostics.DiagnosticRelatedInformation rInfo =
                        ((RedeclaredVarDiagnostic) diagnostic).relatedInformation();
                DiagnosticRelatedInformation relatedInfo =
                        new DiagnosticRelatedInformation();
                relatedInfo.setMessage(rInfo.message());
                relatedInfo.setLocation(getRelatedInfoLocation(rInfo, project.get()));
            }
            if (diagnostics.containsKey(diagPath)) {
                diagnostics.get(diagPath).add(computedDiag);
            } else {
                List<Diagnostic> diags = new ArrayList<>();
                diagnostics.put(diagPath, diags);
            }
        });
        
        /*
        Go through the previously published diagnostics
        and clear the diagnostics associated with a 
        particular file uri by setting an empty list.
         */
        this.previousDiagnostics.forEach((diagPath, diagnosticList) -> {
            if (!diagnostics.containsKey(diagPath)) {
                diagnostics.put(diagPath, new ArrayList<>());
            }
        });
        diagnostics.forEach((diagPath, diagList) -> {
            PublishDiagnosticsParams params =
                    new PublishDiagnosticsParams();
            params.setDiagnostics(diagList);
            URI uri = project.get().sourceRoot().resolve(diagPath).toUri();
            params.setUri(uri.toString());

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

    private Location getRelatedInfoLocation(io.ballerina.tools.diagnostics.DiagnosticRelatedInformation info,
                                            Project project) {
        return null;
    }
}
