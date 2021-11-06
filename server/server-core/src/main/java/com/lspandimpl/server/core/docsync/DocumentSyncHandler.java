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
package com.lspandimpl.server.core.docsync;

import io.ballerina.projects.Project;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import com.lspandimpl.server.api.context.BaseOperationContext;

import java.util.Optional;

/**
 * Document synchronization handler provides a set of APIs to maintain an in-memory project models of the LS.
 * 
 * @since 1.0.0
 */
public interface DocumentSyncHandler {
    Optional<Project> didOpen(DidOpenTextDocumentParams params, BaseOperationContext context);

    Optional<Project> didChange(DidChangeTextDocumentParams params, BaseOperationContext context);

    void didClose(DidCloseTextDocumentParams params, BaseOperationContext context);
}
