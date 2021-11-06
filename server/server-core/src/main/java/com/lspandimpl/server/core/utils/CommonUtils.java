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
package com.lspandimpl.server.core.utils;

import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.SymbolTag;
import com.lspandimpl.server.api.context.BalPosBasedContext;
import com.lspandimpl.server.api.context.BaseOperationContext;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    public static SymbolInformation getSymbolInformation(Symbol symbol, BaseOperationContext context, Path projRoot) {
        SymbolInformation symbolInformation = new SymbolInformation();
        symbolInformation.setName(symbol.getName().get());
        List<SymbolTag> tags = new ArrayList<>();
        SymbolKind kind;
        switch (symbol.kind()) {
            case FUNCTION:
                kind = SymbolKind.Function;
                FunctionSymbol functionSymbol = (FunctionSymbol) symbol;
                Optional<AnnotationSymbol> deprecated = functionSymbol.annotations().stream()
                        .filter(annotationSymbol -> annotationSymbol.getName().orElse("").equals("deprecated"))
                        .findAny();
                if (deprecated.isPresent()) {
                    tags.add(SymbolTag.Deprecated);
                }
                break;
            case CLASS:
                kind = SymbolKind.Class;
                break;
            case ENUM:
                kind = SymbolKind.Enum;
                break;
            case TYPE_DEFINITION:
                kind = SymbolKind.TypeParameter;
                break;
            default:
                kind = null;
                break;
        }
        if (kind != null) {
            symbolInformation.setKind(kind);
        }
        symbolInformation.setTags(tags);
        symbolInformation.setLocation(toLspLocation(context, projRoot, symbol));
        return symbolInformation;
    }

    private static Location toLspLocation(BaseOperationContext context, Path projRoot, Symbol symbol) {
        io.ballerina.tools.diagnostics.Location location = symbol.getLocation().get();
        Location defLocation = new Location();
        Range range = toRange(location.lineRange());
        defLocation.setRange(range);
        defLocation.setUri(getUri(context, projRoot, symbol, location));

        return defLocation;
    }

    public static Position toPosition(LinePosition linePosition) {
        Position position = new Position();
        position.setLine(linePosition.line());
        position.setCharacter(linePosition.offset());

        return position;
    }

    public static Range toRange(LineRange lineRange) {
        Range range = new Range();
        range.setStart(toPosition(lineRange.startLine()));
        range.setEnd(toPosition(lineRange.endLine()));

        return range;
    }

    private static String getUri( BaseOperationContext context, Path projRoot, Symbol symbol,
                                 io.ballerina.tools.diagnostics.Location location) {
        Optional<Project> project = context.compilerManager().getProject(projRoot);
        String moduleName = symbol.getModule().orElseThrow().id().moduleName();

        String packageName = project.get().currentPackage().packageName().value();
        moduleName = moduleName.replace(packageName + ".", "");
        Path definitionPath = projRoot.resolve("modules").resolve(moduleName)
                .resolve(location.lineRange().filePath());
        return definitionPath.toUri().toString();
    }

    public static org.eclipse.lsp4j.Diagnostic toDiagnostic(Diagnostic diagnostic) {
        org.eclipse.lsp4j.Diagnostic diag = new org.eclipse.lsp4j.Diagnostic();
        DiagnosticInfo diagnosticInfo = diagnostic.diagnosticInfo();
        diag.setCode(diagnosticInfo.code());
        diag.setMessage(diagnostic.message());
        diag.setRange(toRange(diagnostic.location().lineRange()));
        switch (diagnosticInfo.severity()) {
            case HINT:
                diag.setSeverity(DiagnosticSeverity.Hint);
                break;
            case INFO:
                diag.setSeverity(DiagnosticSeverity.Information);
                break;
            case ERROR:
                diag.setSeverity(DiagnosticSeverity.Error);
                break;
            case WARNING:
                diag.setSeverity(DiagnosticSeverity.Warning);
                break;
        }

        return diag;
    }
    
    public static String getExpectedTypeDescriptor(Range range, BalPosBasedContext context) {
        LinePosition start = LinePosition.from(range.getStart().getLine(), range.getStart().getCharacter());
        LinePosition end = LinePosition.from(range.getEnd().getLine(), range.getEnd().getCharacter());
        String filePath = context.getPath().toFile().getName();
        LineRange lineRange = LineRange.from(filePath, start, end);
        return context.compilerManager().getSemanticModel(context.getPath()).get().typeOf(lineRange).get().signature();
    }
}
