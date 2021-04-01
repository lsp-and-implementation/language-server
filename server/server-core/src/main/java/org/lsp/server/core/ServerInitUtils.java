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

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.SaveOptions;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.TextDocumentSyncOptions;

import java.util.Arrays;
import java.util.List;

/**
 * Holds the server initializing utilities.
 * 
 * @since 1.0.0
 */
public class ServerInitUtils {
    private ServerInitUtils() {
    }

    /**
     * Get the document sync options.
     * 
     * @return {@link TextDocumentSyncOptions}
     */
    public static TextDocumentSyncOptions getDocumentSyncOption() {
        TextDocumentSyncOptions syncOptions = new TextDocumentSyncOptions();
        SaveOptions saveOptions = new SaveOptions(true);
        syncOptions.setChange(TextDocumentSyncKind.Full);
        syncOptions.setOpenClose(true);
        syncOptions.setWillSave(true);
        syncOptions.setWillSaveWaitUntil(true);
        syncOptions.setSave(saveOptions);

        return syncOptions;
    }

    /**
     * Get the options for the textDocument/completion request.
     * 
     * @return {@link CompletionOptions}
     */
    public static CompletionOptions getCompletionOptions() {
        CompletionOptions completionOptions = new CompletionOptions();
        
        // List of trigger characters.
        List<String> triggerCharacters = Arrays.asList(".", ">");
        
        completionOptions.setResolveProvider(true);
        completionOptions.setTriggerCharacters(triggerCharacters);
        completionOptions.setWorkDoneProgress(true);
        
        return completionOptions;
    }
}