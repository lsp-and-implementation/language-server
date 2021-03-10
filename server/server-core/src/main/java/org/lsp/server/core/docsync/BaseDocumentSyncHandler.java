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
package org.lsp.server.core.docsync;

import io.ballerina.projects.Project;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.services.LanguageClient;
import org.lsp.server.core.compiler.manager.BallerinaCompilerManager;
import org.lsp.server.core.utils.CommonUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Base implementation for the {@link DocumentSyncHandler}.
 *
 * @since 1.0.0
 */
public class BaseDocumentSyncHandler implements DocumentSyncHandler {

    private final BallerinaCompilerManager compilerManager;

    public BaseDocumentSyncHandler(LanguageClient client) {
        this.compilerManager = new BallerinaCompilerManager(client);
    }

    @Override
    public Optional<Project> didOpen(DidOpenTextDocumentParams params) {
        TextDocumentItem textDocument = params.getTextDocument();
        return compilerManager.openDocument(CommonUtils.uriToPath(textDocument.getUri()));
    }

    @Override
    public Optional<Project> didChange(DidChangeTextDocumentParams params) {
        VersionedTextDocumentIdentifier textDocument = params.getTextDocument();
        Path path = CommonUtils.uriToPath(textDocument.getUri());
        List<TextDocumentContentChangeEvent> contentChanges = params.getContentChanges();
        // Base handler assumes the document sync mode is FULL mode
        TextDocumentContentChangeEvent contentChangeEvent = contentChanges.get(0);
        return this.compilerManager.updateDocument(path, contentChangeEvent.getText());
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {

    }
}
