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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.lsp.server.api.context.BalWorkspaceContext;
import org.lsp.server.api.context.LSContext;
import org.lsp.server.core.configdidchange.ConfigurationHolder;
import org.lsp.server.core.contexts.ContextBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BalWorkspaceService implements WorkspaceService {
    private final LSContext lsServerContext;

    public BalWorkspaceService(LSContext lsServerContext) {
        this.lsServerContext = lsServerContext;
    }

    @Override
    public void didChangeConfiguration(
            DidChangeConfigurationParams params) {
        JsonObject settings = (JsonObject) params.getSettings();
        JsonElement configSection =
                settings.get(ConfigurationHolder.CONFIG_SECTION);
        if (configSection != null) {
            ConfigurationHolder.getInstance().update(configSection);
        }
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        // Handle the codelens refresh in chapter 7
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
        return null;
    }

    @Override
    public void didChangeWorkspaceFolders(DidChangeWorkspaceFoldersParams params) {
        BalWorkspaceContext context = ContextBuilder.getWorkspaceContext(this.lsServerContext);
        List<WorkspaceFolder> added = params.getEvent().getAdded();
        params.getEvent().getRemoved();
    }
}
