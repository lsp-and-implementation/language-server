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

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.DocumentOnTypeFormattingRegistrationOptions;
import org.eclipse.lsp4j.OnTypeFormattingCapabilities;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.lsp.server.api.context.LSContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
        List<Registration> regList = Collections.singletonList(reg);
        RegistrationParams regParams = new RegistrationParams(regList);

        // Send the register request and ignore the void result
        serverContext.getClient().registerCapability(regParams);
    }

    private enum Method {
        ON_TYPE_FORMATTING("textDocument/OnTypeFormatting");

        private String name;

        Method(String id) {
            this.name = id;
        }

        public String getName() {
            return name;
        }
    }
}
