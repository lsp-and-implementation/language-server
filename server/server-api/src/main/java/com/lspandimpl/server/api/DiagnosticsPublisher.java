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
package com.lspandimpl.server.api;

import com.lspandimpl.server.api.context.BaseOperationContext;

import java.nio.file.Path;

/**
 * Diagnostics publisher API allows the server-wide diagnostic publishing capability.
 *
 * @since 1.0.0
 */
public interface DiagnosticsPublisher {

    /**
     * Publish project diagnostics.
     *
     * @param path {@link Path} of the ballerina document
     * @param context Operation context
     */
    void publish(BaseOperationContext context, Path path);
}
