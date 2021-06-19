package org.lsp.server.core.completion;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.text.LinePosition;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.lsp.server.api.context.BalCompletionContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModulePartNodeContextProvider6_8 extends
        BalCompletionProviderImpl<ModulePartNode> {
    private final String lineSeparator = System.lineSeparator();

    public ModulePartNodeContextProvider6_8() {
        super(ModulePartNode.class);
    }

    @Override
    public List<CompletionItem>
    getCompletions(ModulePartNode node,
                   BalCompletionContext context) {
        List<CompletionItem> items = new ArrayList<>(this.getTypeCompletionItems(context));
        items.add(getMainFunctionSnippet(context));
        this.sort(node, context, items);
        return items;
    }

    @Override
    public void sort(ModulePartNode node, BalCompletionContext context,
                     List<CompletionItem> items) {
        items.forEach(completionItem -> {
            if (completionItem.getKind()
                    == CompletionItemKind.Snippet) {
                // Snippets are given the highest priority
                completionItem.setSortText("A");
            } else {
                completionItem.setSortText("B");
            }
        });
    }

    @Override
    public void sort(ModulePartNode node, BalCompletionContext context, CompletionItem item, Symbol symbol) {

    }

    private CompletionItem getMainFunctionSnippet(BalCompletionContext context) {
        String ioModuleOrg = "ballerina";
        String ioModuleAlias = "io";
        CompletionItem item = new CompletionItem();
        String template = "public function main() {" + lineSeparator
                + "\tio:println(\"Hello World!!\");"
                + lineSeparator + "}";
        item.setInsertText(template);
        item.setLabel("public main function");
        if (!isModuleImported(ioModuleOrg, ioModuleAlias, context)) {
            /*
            Range starts from the last import statement if there are 
            other imports, otherwise 0,0 is chosen
             */
            Range range = getAutoImportRange(context);

            TextEdit autoImport =
                    getAutoImportTextEdit("ballerina", "io", range);
            item.setAdditionalTextEdits(
                    Collections.singletonList(autoImport));
            item.setKind(CompletionItemKind.Snippet);
        }
        
        return item;
    }

    private boolean isModuleImported(String moduleOrg, String moduleAlias, BalCompletionContext context) {
        Optional<SyntaxTree> syntaxTree = context.compilerManager().getSyntaxTree(context.getPath());
        if (syntaxTree.isEmpty()) {
            return false;
        }
        NodeList<ImportDeclarationNode> imports = ((ModulePartNode) syntaxTree.get().rootNode()).imports();
        return imports.stream().anyMatch(importDecl -> {
            Optional<ImportOrgNameNode> orgName = importDecl.orgName();
            String modName = importDecl.moduleName().stream()
                    .map(Token::text)
                    .collect(Collectors.joining("."));

            return orgName.isPresent() && orgName.get().orgName().text().equals(moduleOrg) && modName.equals(moduleAlias);
        });
    }

    private Range getAutoImportRange(BalCompletionContext context) {
        SyntaxTree syntaxTree = context.compilerManager().getSyntaxTree(context.getPath()).orElseThrow();
        NodeList<ImportDeclarationNode> imports = ((ModulePartNode) syntaxTree.rootNode()).imports();
        Range range = new Range();
        Position start = new Position();
        Position end = new Position();
        
        if (imports.isEmpty()) {
            start.setLine(0);
            start.setCharacter(0);
            end.setLine(0);
            end.setCharacter(0);
        } else {
            ImportDeclarationNode lastImport = imports.get(imports.size());
            LinePosition linePosition = lastImport.lineRange().endLine();
            start.setLine(linePosition.line());
            start.setCharacter(linePosition.offset());
            end.setLine(linePosition.line());
            end.setCharacter(linePosition.offset());
        }
        range.setStart(start);
        range.setEnd(end);
        
        return range;
    }

    private TextEdit getAutoImportTextEdit(String orgName, String moduleName, Range importStmtRange) {
        TextEdit edit = new TextEdit();
        edit.setRange(importStmtRange);
        edit.setNewText("import " + orgName + "/" + moduleName + ";");
        
        return edit;
    }

    @Override
    public boolean enabled() {
        return true;
    }
}
