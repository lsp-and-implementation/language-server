package com.lspandimpl.server.core.contexts;

import com.lspandimpl.server.api.context.BalCallHierarchyOutgoingContext;
import com.lspandimpl.server.api.context.BalCodeActionContext;
import com.lspandimpl.server.api.context.BalCodeLensContext;
import com.lspandimpl.server.api.context.BalCompletionContext;
import com.lspandimpl.server.api.context.BalCompletionResolveContext;
import com.lspandimpl.server.api.context.BalDeclarationContext;
import com.lspandimpl.server.api.context.BalDefinitionContext;
import com.lspandimpl.server.api.context.BalDocumentHighlightContext;
import com.lspandimpl.server.api.context.BalDocumentLinkContext;
import com.lspandimpl.server.api.context.BalDocumentSymbolContext;
import com.lspandimpl.server.api.context.BalFoldingRangeContext;
import com.lspandimpl.server.api.context.BalGotoImplContext;
import com.lspandimpl.server.api.context.BalHoverContext;
import com.lspandimpl.server.api.context.BalLinkedEditingRangeContext;
import com.lspandimpl.server.api.context.BalReferencesContext;
import com.lspandimpl.server.api.context.BalRenameContext;
import com.lspandimpl.server.api.context.BalSemanticTokenDeltaContext;
import com.lspandimpl.server.api.context.BalSemanticTokenRangeContext;
import com.lspandimpl.server.api.context.BalSignatureContext;
import com.lspandimpl.server.api.context.BalTypeDefContext;
import com.lspandimpl.server.api.context.BalWorkspaceContext;
import com.lspandimpl.server.api.context.BaseOperationContext;
import org.eclipse.lsp4j.CallHierarchyItem;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DeclarationParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DocumentColorParams;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.DocumentLinkParams;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.FoldingRangeRequestParams;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.ImplementationParams;
import org.eclipse.lsp4j.LinkedEditingRangeParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PrepareRenameParams;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SelectionRangeParams;
import org.eclipse.lsp4j.SemanticTokensDeltaParams;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SemanticTokensRangeParams;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.TypeDefinitionParams;
import com.lspandimpl.server.api.context.BalDocumentColourContext;
import com.lspandimpl.server.api.context.BalPosBasedContext;
import com.lspandimpl.server.api.context.BalPrepareRenameContext;
import com.lspandimpl.server.api.context.BalSelectionRangeContext;
import com.lspandimpl.server.api.context.BalSemanticTokenContext;
import com.lspandimpl.server.api.context.BalTextDocumentContext;
import com.lspandimpl.server.api.context.LSContext;

public class ContextBuilder {
    public static BaseOperationContext baseContext(LSContext serverContext) {
        return new BaseOperationContextImpl(serverContext);
    }

    public static BalCompletionContext completionContext(LSContext serverContext, CompletionParams params) {
        return new BalCompletionContextImpl(serverContext, params);
    }

    public static BalCompletionResolveContext completionResolveContext(LSContext serverContext,
                                                                       CompletionItem unresolved) {
        return new BalCompletionResolveContextImpl(serverContext, unresolved);
    }

    public static BalRenameContext renameContext(LSContext serverContext, RenameParams params) {
        return new BalRenameContextImpl(serverContext, params);
    }

    public static BalPrepareRenameContext prepareRenameContext(LSContext serverContext, PrepareRenameParams params) {
        return new BalPrepareRenameContextImpl(serverContext, params);
    }

    public static BalDocumentSymbolContext documentSymbolContext(LSContext context, DocumentSymbolParams params) {
        return new BalDocumentSymbolContextImpl(context, params);
    }

    public static BalDocumentHighlightContext documentHighlightContext(LSContext context,
                                                                       DocumentHighlightParams params) {
        return new BalDocumentHighlightContextImpl(context, params);
    }

    public static BalDocumentLinkContext documentLinkContext(LSContext context,
                                                             DocumentLinkParams params) {
        return new BalDocumentLinkContextImpl(context, params);
    }

    public static BalSemanticTokenContext semanticTokensContext(LSContext serverContext, SemanticTokensParams params) {
        return new BalSemanticTokenContextImpl(serverContext, params);
    }

    public static BalSemanticTokenRangeContext semanticTokensRangeContext(LSContext serverContext, SemanticTokensRangeParams params) {
        return new BalSemanticTokenRangeContextImpl(serverContext, params);
    }

    public static BalSemanticTokenDeltaContext semanticTokensDeltaContext(LSContext serverContext, SemanticTokensDeltaParams params) {
        return null;
    }

    public static BalLinkedEditingRangeContext getLinkedEditingRangeContext(LSContext serverContext,
                                                                            LinkedEditingRangeParams params) {
        return new BalLinkedEditingRangeContextImpl(serverContext,
                params.getTextDocument().getUri(),
                params.getPosition());
    }

    public static BalDocumentColourContext getColourContext(LSContext serverContext, DocumentColorParams params) {
        return new BalDocumentColourContextImpl(serverContext, params);
    }

    public static BalFoldingRangeContext getFoldingRangeContext(LSContext serverContext, FoldingRangeRequestParams params) {
        return new BalFoldingRangeContextImpl(serverContext, params);
    }
    
    public static BalPosBasedContext getPosBasedContext(LSContext serverContext, String uri, Position position) {
        return new BalPosBasedContextImpl(serverContext, uri, position);
    }
    
    public static BalTextDocumentContext getTextDocumentContext(LSContext serverContext, String uri) {
        return new BalTextDocumentContextImpl(serverContext, uri);
    }
    
    public static BalCallHierarchyOutgoingContext getCallHierarchyOutGoingContext(LSContext serverContext, CallHierarchyItem item) {
        return new BalCallHierarchyOutgoingContextImpl(serverContext, item);
    }
    
    public static BalWorkspaceContext getWorkspaceContext(LSContext serverContext) {
        return new BalWorkspaceContextImpl(serverContext);
    }
    
    public static BalSignatureContext getSignatureContext(LSContext serverContext, SignatureHelpParams params) {
        return new BalSignatureContextImpl(serverContext, params);
    }
    
    public static BalHoverContext getHoverContext(LSContext serverContext, HoverParams params) {
        return new BalHoverContextImpl(serverContext, params.getTextDocument().getUri(), params.getPosition());
    }
    
    public static BalCodeActionContext getCodeActionContext(LSContext serverContext, CodeActionParams params) {
        return new BalCodeActionContextImpl(serverContext, params.getTextDocument().getUri(), params.getRange());
    }
    
    public static BalCodeLensContext getCodeLensContext(LSContext serverContext, CodeLensParams params) {
        return new BalCodeLensContextImpl(serverContext, params.getTextDocument().getUri());
    }
    
    public static BalReferencesContext getReferencesContext(LSContext serverContext, ReferenceParams params) {
        return new BalReferencesContextImpl(serverContext, params);
    }
    
    public static BalDefinitionContext getDefinitionContext(LSContext serverContext, DefinitionParams params) {
        String uri = params.getTextDocument().getUri();
        Position position = params.getPosition();
        
        return new BalDefinitionContextImpl(serverContext, uri, position);
    }
    
    public static BalDeclarationContext getDeclarationContext(LSContext serverContext, DeclarationParams params) {
        String uri = params.getTextDocument().getUri();
        Position position = params.getPosition();
        
        return new BalDeclarationContextImpl(serverContext, uri, position);
    }
    
    public static BalTypeDefContext getTypeDefinitionContext(LSContext serverContext, TypeDefinitionParams params) {
        String uri = params.getTextDocument().getUri();
        Position position = params.getPosition();
        
        return new BalTypeDefinitionContextImpl(serverContext, uri, position);
    }
    
    public static BalGotoImplContext getGotoImplContext(LSContext serverContext, ImplementationParams params) {
        String uri = params.getTextDocument().getUri();
        Position position = params.getPosition();
        
        return new BalGotoImplementationContextImpl(serverContext, uri, position);
    }
    
    public static BalSelectionRangeContext getSelectionRangeContext(LSContext serverContext, SelectionRangeParams params) {
        return new BalSelectionRangeContextImpl(serverContext, params);
    }
}
