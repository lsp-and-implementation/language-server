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
package org.lsp.server.core.utils;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import org.eclipse.lsp4j.SymbolInformation;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Common utility methods exposed to use within the language server core implementation.
 *
 * @since 1.0.0
 */
public class CommonUtils {
    public static final String LINE_SEPARATOR = System.lineSeparator();
    public static final String MD_LINE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR;

    private CommonUtils() {
    }

    /**
     * Get the {@link Path} from the given string URI.
     *
     * @param pathUri to convert
     * @return {@link Path}
     */
    public static Path uriToPath(String pathUri) {
        URI uri = URI.create(pathUri);
        return Paths.get(uri);
    }

    public static boolean isKeyword(String name) {
        return name.equals("int") || name.equals("string");
    }

    public static Optional<TypeSymbol> getTypeDefinition(Symbol symbol) {
        TypeSymbol typeSymbol;
        // TODO: Can support other symbol kinds
        switch (symbol.kind()) {
            case VARIABLE:
                typeSymbol = ((VariableSymbol) symbol).typeDescriptor();
                break;
            default:
                typeSymbol = null;
                break;
        }
        if (typeSymbol == null || typeSymbol.typeKind() != TypeDescKind.TYPE_REFERENCE) {
            return Optional.empty();
        }
        return Optional.ofNullable(typeSymbol);

    }

    public static boolean isDeprecated(ModuleMemberDeclarationNode member) {
        return false;
    }

    public static SymbolInformation getSymbolInformation(Symbol symbol) {
        SymbolInformation symbolInformation = new SymbolInformation();

        return symbolInformation;
    }
}
