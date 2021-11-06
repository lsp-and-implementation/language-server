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
package com.lspandimpl.server.api.context;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.Optional;

/**
 * Ballerina language server context information.
 *
 * @since 1.0.0
 */
public interface LSContext {

    <V> void put(LSContext.Key<V> key, V value);

    <V> V get(LSContext.Key<V> key);

    <V> void put(Class<V> clazz, V value);

    <V> V get(Class<V> clazz);

    void setClient(LanguageClient client);

    LanguageClient getClient();

    void setClientCapabilities(ClientCapabilities capabilities);

    Optional<ClientCapabilities> getClientCapabilities();

    /**
     * @param <K> key
     * @since 1.0.0
     */
    class Key<K> {
    }
}
