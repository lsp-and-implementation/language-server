package com.lspandimpl.server.core.extensions.languages;

import com.lspandimpl.server.api.context.LSContext;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class LanguageFeatureExtensionDelegator {
    private static final LanguageFeatureExtensionDelegator INSTANCE
            = new LanguageFeatureExtensionDelegator();
    private final List<CompletionFeatureExtension> completionExtensions =
            new ArrayList<>();
    
    /**
     * Get the completions.
     *
     * @param params completion parameters
     * @return {@link Either} completion results
     * @throws Throwable while executing the extension
     */
    public Either<List<CompletionItem>, CompletionList>
    completion(CompletionParams params, LSContext serverContext)
            throws Throwable {
        List<CompletionItem> completionItems = new ArrayList<>();
        for (CompletionFeatureExtension ext : completionExtensions) {
            if (ext.validate(params)) {
                completionItems.addAll(ext.completion(params, serverContext));
            }
        }

        return Either.forLeft(completionItems);
    }
    
    private LanguageFeatureExtensionDelegator() {
        this.loadCompletionExtensions();
    }
    
    private void loadCompletionExtensions() {
        ServiceLoader.load(CompletionFeatureExtension.class)
                .forEach(completionExtensions::add);
    }
}
