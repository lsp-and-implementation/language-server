package org.lsp.server.core.extensions.languages;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;
import org.lsp.server.api.context.LSContext;

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
