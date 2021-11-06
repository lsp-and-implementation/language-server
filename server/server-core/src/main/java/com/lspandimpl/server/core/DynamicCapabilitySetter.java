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

import io.ballerina.projects.util.ProjectConstants;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CompletionCapabilities;
import org.eclipse.lsp4j.CompletionRegistrationOptions;
import org.eclipse.lsp4j.DocumentFilter;
import org.eclipse.lsp4j.DocumentOnTypeFormattingRegistrationOptions;
import org.eclipse.lsp4j.OnTypeFormattingCapabilities;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.TextDocumentChangeRegistrationOptions;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import com.lspandimpl.server.api.context.LSContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A capability setting utility which allows the language server to register capabilities dynamically.
 *
 * @since 1.0.0
 */
public class DynamicCapabilitySetter {
    // TODO: IMPLEMENT ALL THE OPERATIONS HERE
    private static final LSContext.Key<DynamicCapabilitySetter> CAPABILITY_SETTER_KEY = new LSContext.Key<>();

    private DynamicCapabilitySetter(LSContext serverContext) {
        serverContext.put(CAPABILITY_SETTER_KEY, this);
    }

    /**
     * Get the Dynamic capability setter.
     *
     * @param serverContext Language Server Context
     * @return {@link DynamicCapabilitySetter}
     */
    public static DynamicCapabilitySetter getInstance(LSContext serverContext) {
        DynamicCapabilitySetter capabilitySetter = serverContext.get(CAPABILITY_SETTER_KEY);
        if (capabilitySetter == null) {
            capabilitySetter = new DynamicCapabilitySetter(serverContext);
        }

        return capabilitySetter;
    }

    /**
     * Register the onTypeFormatting capability.
     *
     * @param serverContext language server context
     */
    public void registerOnTypeFormatting(LSContext serverContext) {
        Optional<ClientCapabilities> clientCapabilities = serverContext.getClientCapabilities();

        if (clientCapabilities.isEmpty()) {
            // Client capabilities are not saved
            return;
        }

        OnTypeFormattingCapabilities onTypeFormatting =
                clientCapabilities.get().getTextDocument().getOnTypeFormatting();
        if (!onTypeFormatting.getDynamicRegistration()) {
            /*
            client does not support dynamic registration for 
             ontype formatting. Gracefully fall back
             */
            return;
        }
        // Generate the registration options
        DocumentOnTypeFormattingRegistrationOptions opts = new DocumentOnTypeFormattingRegistrationOptions();
        opts.setFirstTriggerCharacter("}");
        List<String> otherTriggers = Collections.singletonList(";");
        opts.setMoreTriggerCharacter(otherTriggers);

        String method = Method.ON_TYPE_FORMATTING.getName();
        /*
        We use method name for both id and the method and we use the same
        for unregister a capability
         */
        Registration reg = new Registration(method, method);
        reg.setRegisterOptions(opts);
        List<Registration> regList = Collections.singletonList(reg);
        RegistrationParams regParams = new RegistrationParams(regList);

        // Send the register request and ignore the void result
        serverContext.getClient().registerCapability(regParams);
    }

    public void registerBallerinaTomlCompletion(LSContext serverContext) {
        Optional<ClientCapabilities> clientCapabilities =
                serverContext.getClientCapabilities();

        if (clientCapabilities.isEmpty()) {
            // Client capabilities are not saved
            return;
        }
        CompletionCapabilities completionCapabilities =
                clientCapabilities.get().getTextDocument().getCompletion();
        if (!completionCapabilities.getDynamicRegistration()) {
        /*
        client does not support dynamic registration for 
         completion. Gracefully fall back
         */
            return;
        }
        CompletionRegistrationOptions options =
                new CompletionRegistrationOptions();
        DocumentFilter tomlFilter = new DocumentFilter();
        tomlFilter.setLanguage("toml");
        tomlFilter.setScheme("file");
        tomlFilter.setPattern("/**/" + ProjectConstants.BALLERINA_TOML);

        options.setResolveProvider(true);
        options.setDocumentSelector(Collections.singletonList(tomlFilter));
        String method = Method.COMPLETION.getName();

        Registration reg = new Registration(method, method);
        List<Registration> regList = Collections.singletonList(reg);
        RegistrationParams regParams = new RegistrationParams(regList);

//        return regParams;
    }
    
    public void registerTextDocumentSyncOptions(LSContext serverContext) {
        TextDocumentChangeRegistrationOptions registrationOptions = new TextDocumentChangeRegistrationOptions();
        
        DocumentFilter txtDocFilter = new DocumentFilter();
        txtDocFilter.setLanguage("text");
        txtDocFilter.setScheme("file");
                
        registrationOptions.setDocumentSelector(Collections.singletonList(txtDocFilter));
        registrationOptions.setSyncKind(TextDocumentSyncKind.Full);

        // Once registered we will receive didOpen notification for .txt files
        // For the demonstration purposes we will show a notification for now
        Registration didOpenRegistration = new Registration();
        didOpenRegistration.setRegisterOptions(registrationOptions);
        didOpenRegistration.setMethod("textDocument/didOpen");
        didOpenRegistration.setId(UUID.randomUUID().toString());

        // Once registered we will receive didChange notification for .txt files
        // For the demonstration purposes we will show a notification for now
        Registration didChangeRegistration = new Registration();
        didChangeRegistration.setRegisterOptions(registrationOptions);
        didChangeRegistration.setMethod("textDocument/didChange");
        didChangeRegistration.setId(UUID.randomUUID().toString());
        
        RegistrationParams registrationParams = new RegistrationParams();
        registrationParams.setRegistrations(Arrays.asList(didOpenRegistration, didChangeRegistration));
        
        serverContext.getClient().registerCapability(registrationParams);
    }

    private enum Method {
        ON_TYPE_FORMATTING("textDocument/onTypeFormatting"),
        COMPLETION("textDocument/completion");

        private String name;

        Method(String id) {
            this.name = id;
        }

        public String getName() {
            return name;
        }
    }
}
