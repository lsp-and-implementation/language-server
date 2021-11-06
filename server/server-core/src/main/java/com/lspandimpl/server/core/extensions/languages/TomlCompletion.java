package com.lspandimpl.server.core.extensions.languages;

import com.lspandimpl.server.api.context.LSContext;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;

import java.util.List;

public class TomlCompletion implements CompletionFeatureExtension {
    @Override
    public List<CompletionItem> completion(CompletionParams params, LSContext serverContext) {
        return null;
    }

    @Override
    public boolean validate(CompletionParams params) {
        String uri = params.getTextDocument().getUri();
        return uri.endsWith("Ballerina.toml");
    }
}
