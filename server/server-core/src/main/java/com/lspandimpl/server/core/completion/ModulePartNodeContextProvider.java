package com.lspandimpl.server.core.completion;

import com.lspandimpl.server.api.context.BalCompletionContext;
import com.lspandimpl.server.core.completion.resolve.AutoImportTextEditData;
import com.lspandimpl.server.core.completion.utils.TextEditGenerator;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModulePartNodeContextProvider extends
        BalCompletionProviderImpl<ModulePartNode> {
    private final String lineSeparator = System.lineSeparator();

    public ModulePartNodeContextProvider() {
        super(ModulePartNode.class);
    }

    @Override
    public List<CompletionItem>
    getCompletions(ModulePartNode node,
                   BalCompletionContext context) {
        List<CompletionItem> items = new ArrayList<>(this.getTypeCompletionItems(context));
        items.add(getMainFunctionSnippet(context));
        items.add(getHttpServiceSnippet(context));
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

    private CompletionItem
    getMainFunctionSnippet(BalCompletionContext context) {
        String ioModuleOrg = "ballerina";
        String ioModuleAlias = "io";
        CompletionItem item = new CompletionItem();
        String template = "public function main() {" + lineSeparator
                + "\tio:println(\"Hello World!!\");"
                + lineSeparator + "}";
        item.setInsertText(template);
        item.setLabel("main function");
        SyntaxTree syntaxTree = context.compilerManager()
                .getSyntaxTree(context.getPath()).orElseThrow();
        if (!TextEditGenerator.isModuleImported(ioModuleOrg, ioModuleAlias, syntaxTree)) {
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
        item.setFilterText("main");

        return item;
    }

    private CompletionItem getHttpServiceSnippet(BalCompletionContext context) {
        CompletionItem item = new CompletionItem();
        String template = "service /${1} on new http:Listener(8080) {"
                + lineSeparator + "\tresource function ${2:get} ${3:getResource}"
                + "(http:Caller ${4:caller}, http:Request ${5:req}) {" + lineSeparator
                + "\t\t" + lineSeparator + "\t}" + lineSeparator + "}";
        item.setInsertText(template);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setLabel("service - http");
        item.setFilterText("service");
        SyntaxTree syntaxTree = context.compilerManager()
                .getSyntaxTree(context.getPath()).orElseThrow();
        List<String> properties = context.clientCapabilities().getTextDocument().getCompletion()
                .getCompletionItem().getResolveSupport().getProperties();
        String importStmt = "import ballerina/http;";
        if (properties.contains("additionalTextEdits")) {
            // proceed with resolve and set data
            String uri = context.getPath().toUri().toString();
            AutoImportTextEditData data =
                    new AutoImportTextEditData(uri, importStmt);
            item.setData(data);
        } else {
            TextEdit autoImport = TextEditGenerator
                    .getAutoImport(importStmt, syntaxTree);
            item.setAdditionalTextEdits(
                    Collections.singletonList(autoImport));
        }

        return item;
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
            ImportDeclarationNode lastImport = imports.get(imports.size() - 1);
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
