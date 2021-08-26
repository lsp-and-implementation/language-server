package org.lsp.server.core.documentlink;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Range;
import org.lsp.server.api.context.BalDocumentLinkContext;
import org.lsp.server.api.context.BaseOperationContext;
import org.lsp.server.core.AbstractProvider;
import org.lsp.server.core.utils.CommonUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DocumentLinkProvider extends AbstractProvider {
    private static final String DOC_URI = "uri";
    private DocumentLinkProvider() {
    }
    
public static List<DocumentLink>
getDocumentLink(BalDocumentLinkContext context) {
    Map<LineRange, String> linkRanges = getLinkRanges(context);
    List<DocumentLink> documentLinks = new ArrayList<>();
    /*
    Here we set the target. Target can also be 
    calculated from the resolve request
     */
    linkRanges.forEach((lineRange, target) -> {
        DocumentLink link = new DocumentLink();
        if (context.clientCapabilities().getTextDocument()
                .getDocumentLink().getTooltipSupport()) {
            link.setTooltip("cmd/ ctrl + click to navigate");
        }
        Range range = toRange(lineRange);
        link.setRange(range);
        /*
        Preserve the document URI in the data field.
        This will be referred during the resolve request 
         */
        // Map<String, String> dataMap = new HashMap<>();
        // dataMap.put(DOC_URI,
        // context.params().getTextDocument().getUri());
        // link.setData(dataMap);
        link.setTarget(target);
        documentLinks.add(link);
    });
    
    return documentLinks;
}

public static DocumentLink
getDocumentLinkResolved(BaseOperationContext context, DocumentLink documentLink) {
    /*
    Extract the document Uri from data field
     */
    Map<String, String> data = (Map<String, String>) documentLink.getData();
    String uri = data.get(DOC_URI);
    Path path = CommonUtils.uriToPath(uri);
    
    String target = getTarget(context, path, documentLink.getRange());
    DocumentLink link = new DocumentLink();
    link.setTooltip(documentLink.getTooltip());
    link.setRange(documentLink.getRange());
    link.setTooltip(documentLink.getTooltip());
    link.setTarget(target);
    
    return link;
}

    private static String getTarget(BaseOperationContext context, Path path, Range range) {
        return "http://localhost";
    }

    private static Map<LineRange, String> getLinkRanges(BalDocumentLinkContext context) {
        Optional<SyntaxTree> syntaxTree = context.compilerManager().getSyntaxTree(context.getPath());
        if (syntaxTree.isEmpty()) {
            return Collections.emptyMap();
        }
        ModulePartNode modulePartNode = syntaxTree.get().rootNode();
        DocumentLinkVisitor linkVisitor = new DocumentLinkVisitor();
        modulePartNode.accept(linkVisitor);

        return linkVisitor.getLinkRanges();
    }
}
