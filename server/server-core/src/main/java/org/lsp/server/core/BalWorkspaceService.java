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

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;

public class BalWorkspaceService implements WorkspaceService {
    
    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams params) {
    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        // Handle the codelens refresh in chapter 7
    }
}
