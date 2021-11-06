package com.lspandimpl.server.core.semantictoken;

import com.lspandimpl.server.api.context.BalSemanticTokenRangeContext;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.text.LinePosition;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SemanticTokenModifiers;
import org.eclipse.lsp4j.SemanticTokenTypes;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensLegend;
import com.lspandimpl.server.api.context.BalSemanticTokenContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemanticTokensProvider {
    private static final List<String> TOKEN_TYPES =
            Arrays.asList(
                    SemanticTokenTypes.Type,
                    SemanticTokenTypes.Enum
            );
    private static final List<String> MODIFIERS =
            Arrays.asList(
                    SemanticTokenModifiers.Declaration,
                    SemanticTokenModifiers.Definition
            );
    public static final SemanticTokensLegend SEMANTIC_TOKENS_LEGEND =
            new SemanticTokensLegend(TOKEN_TYPES, MODIFIERS);

    public static SemanticTokens getSemanticTokens(BalSemanticTokenContext context) {
        SyntaxTree syntaxTree = context.currentSyntaxTree().get();
        List<Integer> data = new ArrayList<>();
        Map<Integer, Token> lastTokenInLine = new HashMap<>();
        int lastLine = 0;
        ModulePartNode modPart = syntaxTree.rootNode();
        for (ModuleMemberDeclarationNode member : (modPart).members()) {
            // filter only the type definitions
            if (member.kind() != SyntaxKind.TYPE_DEFINITION) {
                continue;
            }
            Token typeName = ((TypeDefinitionNode) member).typeName();
            LinePosition startLine =
                    typeName.lineRange().startLine();
            int startChar = startLine.offset();
            if (lastTokenInLine.containsKey(startLine.line())) {
                // captures the token offset relative
                // to last token of a given line
                startChar = startChar - lastTokenInLine
                        .get(startLine.line()).lineRange()
                        .startLine().offset();
            }
            int line = startLine.line() - lastLine;
            lastLine = startLine.line();
            lastTokenInLine.put(line, typeName);
            int length = typeName.text().length();
            int tokenType = TOKEN_TYPES.indexOf(SemanticTokenTypes.Type);
            int tokenModifiers =
                    (1 << MODIFIERS.indexOf(SemanticTokenModifiers.Declaration))
                            | (1 << MODIFIERS.indexOf(SemanticTokenModifiers.Definition));
            data.add(line);
            data.add(startChar);
            data.add(length);
            data.add(tokenType);
            data.add(tokenModifiers);
        }

        return new SemanticTokens(data);
    }

    public static SemanticTokens getSemanticTokensInRange(BalSemanticTokenRangeContext context) {
        Range range = context.params().getRange();
        SyntaxTree syntaxTree = context.compilerManager().getSyntaxTree(context.getPath()).orElseThrow();
        List<Integer> data = new ArrayList<>();
        Map<Integer, Token> lastTokenInLine = new HashMap<>();
        int lastLine = 0;
        for (ModuleMemberDeclarationNode member : ((ModulePartNode) syntaxTree.rootNode()).members()) {
            if (member.kind() == SyntaxKind.TYPE_DEFINITION && withinRange(member, range)) {
                Token typeName = ((TypeDefinitionNode) member).typeName();
                LinePosition startLine = typeName.lineRange().startLine();
                int startChar = startLine.offset();
                if (lastTokenInLine.containsKey(startLine.line())) {
                    startChar = startChar - lastTokenInLine.get(startLine.line()).lineRange().startLine().offset();
                }
                int line = startLine.line() - lastLine;
                lastLine = startLine.line();
                lastTokenInLine.put(line, typeName);
                int length = typeName.text().length();
                int tokenType = TOKEN_TYPES.indexOf(SemanticTokenTypes.Type);
                int tokenModifiers = (1 << MODIFIERS.indexOf(SemanticTokenModifiers.Declaration))
                        | (1 << MODIFIERS.indexOf(SemanticTokenModifiers.Definition));
                data.add(line);
                data.add(startChar);
                data.add(length);
                data.add(tokenType);
                data.add(tokenModifiers);
            }
        }

        return new SemanticTokens(data);
    }

    private static boolean withinRange(ModuleMemberDeclarationNode member, Range range) {
        LinePosition startLine = member.lineRange().startLine();
        LinePosition endLine = member.lineRange().endLine();

        return startLine.line() >= range.getStart().getLine() && endLine.line() <= range.getEnd().getLine();
    }
}
