package com.lspandimpl.server.core.extensions.languages;

import com.lspandimpl.server.api.context.LSContext;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;

import java.util.List;

public interface CompletionFeatureExtension {
    /**
     * Get the completions result.
     * 
     * @param params Completion parameters
     * @return {@link List} completion response
     */
    List<CompletionItem> completion(CompletionParams params, LSContext serverContext);

    /**
     * Validation for the completion trigger.
     * @param params Completion parameters
     * @return {@link Boolean} validation status
     */
    boolean validate(CompletionParams params);
}
