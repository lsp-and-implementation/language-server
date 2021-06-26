package org.lsp.server.core.documentlink;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.tools.diagnostics.Location;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.Range;
import org.lsp.server.api.context.BalDocumentHighlightContext;
import org.lsp.server.api.context.BalDocumentLinkContext;
import org.lsp.server.api.context.BaseOperationContext;
import org.lsp.server.core.utils.CommonUtils;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentLinkProvider {
    private static final String DOC_URI = "uri";
    private DocumentLinkProvider() {
    }
    
public static List<DocumentLink>
getDocumentLink(BalDocumentLinkContext context) {
    List<DocumentLink> links = getLinks(context);
    /*
    Here we do not set the target. Target will be 
    calculated from the resolve request
     */
    for (DocumentLink link : links) {
        if (context.clientCapabilities().getTextDocument().getDocumentLink().getTooltipSupport()) {
            link.setTooltip("cmd/ ctrl + click to navigate");
        }
        Map<String, String> dataMap = new HashMap<>();
        /*
        Preserve the document URI in the data field.
        This will be referred during the resolve request 
         */
        dataMap.put(DOC_URI,
                context.params().getTextDocument().getUri());
        link.setData(dataMap);
    }
    
    return links;
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

    private static List<DocumentLink> getLinks(BalDocumentLinkContext context) {
        return Collections.emptyList();
    }

    private static boolean isWrite(BalDocumentHighlightContext context, Location location) {
        SemanticModel semanticModel = context.compilerManager()
                .getSemanticModel(context.getPath()).orElseThrow();
        
        return location.lineRange().startLine().line() == 13;
    }
}
