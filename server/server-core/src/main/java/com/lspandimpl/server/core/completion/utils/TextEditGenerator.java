package com.lspandimpl.server.core.completion.utils;

import com.lspandimpl.server.core.utils.CommonUtils;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.text.LinePosition;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.Optional;
import java.util.stream.Collectors;

public class TextEditGenerator {
    public static TextEdit getAutoImport(String importStatement, SyntaxTree syntaxTree) {
        NodeList<ImportDeclarationNode> imports = ((ModulePartNode) syntaxTree.rootNode()).imports();
        TextEdit edit = new TextEdit();
        String insertText = importStatement;
        Position start = new Position();
        Position end;
        if (imports.isEmpty()) {
            start.setLine(0);
            start.setLine(0);
        } else {
            ImportDeclarationNode lastImport = imports.get(imports.size() - 1);
            LinePosition endLine = lastImport.lineRange().endLine();
            start.setLine(endLine.line());
            start.setCharacter(endLine.offset());
            
            insertText = CommonUtils.LINE_SEPARATOR + insertText;
        }
        end = start;
        insertText += CommonUtils.LINE_SEPARATOR;

        Range range = new Range(start, end);
        edit.setNewText(insertText);
        edit.setRange(range);
        
        return edit;
    }

    public static boolean isModuleImported(String moduleOrg, String moduleAlias, SyntaxTree syntaxTree) {
        NodeList<ImportDeclarationNode> imports = ((ModulePartNode) syntaxTree.rootNode()).imports();
        return imports.stream().anyMatch(importDecl -> {
            Optional<ImportOrgNameNode> orgName = importDecl.orgName();
            String modName = importDecl.moduleName().stream()
                    .map(Token::text)
                    .collect(Collectors.joining("."));

            return orgName.isPresent() && orgName.get().orgName().text().equals(moduleOrg)
                    && modName.equals(moduleAlias);
        });
    }
}
