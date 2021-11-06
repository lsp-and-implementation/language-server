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
package com.lspandimpl.server.core;

import com.google.gson.JsonObject;
import com.lspandimpl.server.api.context.BalCallHierarchyOutgoingContext;
import com.lspandimpl.server.api.context.BalCodeActionContext;
import com.lspandimpl.server.api.context.BalCodeLensContext;
import com.lspandimpl.server.api.context.BalCompletionContext;
import com.lspandimpl.server.api.context.BalCompletionResolveContext;
import com.lspandimpl.server.api.context.BalDeclarationContext;
import com.lspandimpl.server.api.context.BalDefinitionContext;
import com.lspandimpl.server.api.context.BalDocumentColourContext;
import com.lspandimpl.server.api.context.BalDocumentHighlightContext;
import com.lspandimpl.server.api.context.BalDocumentLinkContext;
import com.lspandimpl.server.api.context.BalDocumentSymbolContext;
import com.lspandimpl.server.api.context.BalFoldingRangeContext;
import com.lspandimpl.server.api.context.BalGotoImplContext;
import com.lspandimpl.server.api.context.BalHoverContext;
import com.lspandimpl.server.api.context.BalLinkedEditingRangeContext;
import com.lspandimpl.server.api.context.BalPosBasedContext;
import com.lspandimpl.server.api.context.BalPrepareRenameContext;
import com.lspandimpl.server.api.context.BalReferencesContext;
import com.lspandimpl.server.api.context.BalRenameContext;
import com.lspandimpl.server.api.context.BalSemanticTokenRangeContext;
import com.lspandimpl.server.api.context.BalSignatureContext;
import com.lspandimpl.server.api.context.BalTextDocumentContext;
import com.lspandimpl.server.api.context.BalTypeDefContext;
import com.lspandimpl.server.api.context.BaseOperationContext;
import com.lspandimpl.server.api.context.LSContext;
import com.lspandimpl.server.ballerina.compiler.workspace.CompilerManager;
import com.lspandimpl.server.core.callhierarchy.CallHierarchyProvider;
import com.lspandimpl.server.core.codeaction.CodeActionProvider;
import com.lspandimpl.server.core.codelens.CodeLensProvider;
import com.lspandimpl.server.core.completion.BalCompletionRouter;
import com.lspandimpl.server.core.completion.CompletionItemResolver;
import com.lspandimpl.server.core.contexts.ContextBuilder;
import com.lspandimpl.server.core.definition.DefinitionProvider;
import com.lspandimpl.server.core.docsymbol.DocumentSymbolProvider;
import com.lspandimpl.server.core.docsync.BaseDocumentSyncHandler;
import com.lspandimpl.server.core.docsync.DocumentSyncHandler;
import com.lspandimpl.server.core.documentlink.DocumentLinkProvider;
import com.lspandimpl.server.core.foldingrange.FoldingRangeProvider;
import com.lspandimpl.server.core.format.FormatProvider;
import com.lspandimpl.server.core.highlight.DocumentHighlightProvider;
import com.lspandimpl.server.core.hover.HoverProvider;
import com.lspandimpl.server.core.linkedediting.LinkedEditingRangeProvider;
import com.lspandimpl.server.core.semantictoken.SemanticTokensProvider;
import com.lspandimpl.server.core.signature.SignatureProvider;
import com.lspandimpl.server.core.utils.CommonUtils;
import com.lspandimpl.server.core.utils.ContextEvaluator;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import org.eclipse.lsp4j.CallHierarchyIncomingCall;
import org.eclipse.lsp4j.CallHierarchyIncomingCallsParams;
import org.eclipse.lsp4j.CallHierarchyItem;
import org.eclipse.lsp4j.CallHierarchyOutgoingCall;
import org.eclipse.lsp4j.CallHierarchyOutgoingCallsParams;
import org.eclipse.lsp4j.CallHierarchyPrepareParams;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.ColorInformation;
import org.eclipse.lsp4j.ColorPresentation;
import org.eclipse.lsp4j.ColorPresentationParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DeclarationParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentColorParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentLinkParams;
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeRequestParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.ImplementationParams;
import org.eclipse.lsp4j.LinkedEditingRangeParams;
import org.eclipse.lsp4j.LinkedEditingRanges;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PrepareRenameParams;
import org.eclipse.lsp4j.PrepareRenameResult;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SelectionRange;
import org.eclipse.lsp4j.SelectionRangeParams;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensDelta;
import org.eclipse.lsp4j.SemanticTokensDeltaParams;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SemanticTokensRangeParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.TypeDefinitionParams;
import org.eclipse.lsp4j.WillSaveTextDocumentParams;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import com.lspandimpl.server.api.context.BalSelectionRangeContext;
import com.lspandimpl.server.api.context.BalSemanticTokenContext;
import com.lspandimpl.server.core.doccolour.DocumentColourProvider;
import com.lspandimpl.server.core.references.ReferencesProvider;
import com.lspandimpl.server.core.rename.RenameProvider;
import com.lspandimpl.server.core.selectionrange.SelectionRangeProvider;
import com.lspandimpl.server.core.utils.TextModifierUtil;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the {@link TextDocumentService}.
 *
 * @since 1.0.0
 */
public class BalTextDocumentService implements TextDocumentService {
    private final DocumentSyncHandler documentSyncHandler;
    private final LSContext serverContext;

    public BalTextDocumentService(LSContext serverContext) {
        this.serverContext = serverContext;
        this.documentSyncHandler = new BaseDocumentSyncHandler(serverContext);
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        Path uriPath = CommonUtils.uriToPath(params.getTextDocument().getUri());
        BaseOperationContext context = ContextBuilder.baseContext(this.serverContext);
        CompilerManager compilerManager = context.compilerManager();
        if (uriPath.toFile().getName().endsWith(".txt")) {
            // Here we notify that we have opened a .txt document.
            // If the server needs special handling the implementation can navigate to the
            // relevant handler from this point
            context.clientLogManager().showInfoMessage("Document opened with `.txt` extension");
            return;
        }
        Optional<Project> projectForPath = compilerManager.getProject(uriPath);
        /*
        If the project already exists in the compiler manager that means
        we have sent the diagnostics for the project earlier.
        Hence we do not need to publish the diagnostics again.
        This will save a significant number of `publishDiagnostic` calls
        for projects with a many files
         */
        if (projectForPath.isEmpty()) {
            Optional<Project> project = this.documentSyncHandler.didOpen(params, context);
            project.ifPresent(prj -> context.diagnosticPublisher().publish(context, uriPath));
        }
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        BaseOperationContext context = ContextBuilder.baseContext(this.serverContext);
        Path uriPath = CommonUtils.uriToPath(params.getTextDocument().getUri());
        if (uriPath.toFile().getName().endsWith(".txt")) {
            // Here we notify that we have changed a .txt document.
            // If the server needs special handling the implementation can navigate to the
            // relevant handler from this point
            context.clientLogManager().showInfoMessage("Document change event for a document with `.txt` extension");
            return;
        }
        Optional<Project> project = this.documentSyncHandler.didChange(params, context);
        Path pathUri = CommonUtils.uriToPath(params.getTextDocument().getUri());
        /*
         Publish the diagnostics upon the changes of the document.
         Even this is a single file change, the semantics can 
         affect the whole project. Therefore we have to publish the 
         diagnostics for the whole project.
         */
        project.ifPresent(prj -> context.diagnosticPublisher().publish(context, pathUri));
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        Path path = CommonUtils.uriToPath(uri);
        BaseOperationContext context = ContextBuilder.baseContext(this.serverContext);
        CompilerManager compilerManager = context.compilerManager();
        Project project = compilerManager.getProject(path).orElseThrow();

        if (project.kind() == ProjectKind.SINGLE_FILE_PROJECT) {
            this.documentSyncHandler.didClose(params, context);
        }
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {

    }

    @Override
    public CompletableFuture<List<TextEdit>>
    willSaveWaitUntil(WillSaveTextDocumentParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BaseOperationContext context =
                    ContextBuilder.baseContext(this.serverContext);
            ClientCapabilities clientCapabilities =
                    this.serverContext.getClientCapabilities().orElseThrow();
            if (!clientCapabilities.getTextDocument()
                    .getSynchronization().getWillSaveWaitUntil()) {
                return null;
            }
            // Here we do not consider the reason property here
            String uri = params.getTextDocument().getUri();
            Path path = CommonUtils.uriToPath(uri);
            Optional<TextEdit> textEdit =
                    TextModifierUtil.withEndingNewLine(path, context);

            return textEdit
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        });
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                BalHoverContext context = ContextBuilder.getHoverContext(this.serverContext, params);
                ContextEvaluator.fillTokenInfoAtCursor(context);
                return HoverProvider.getHover(context);
            } catch (Throwable e) {
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalCompletionContext context = ContextBuilder.completionContext(this.serverContext, params);
            return Either.forLeft(BalCompletionRouter.compute(context));
        });
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
        return CompletableFuture.supplyAsync(() -> {
            BalCompletionResolveContext context = ContextBuilder
                    .completionResolveContext(this.serverContext,
                            unresolved);

            return CompletionItemResolver.resolve(context);
        });
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(SignatureHelpParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalSignatureContext context = ContextBuilder.getSignatureContext(serverContext, params);
            ContextEvaluator.fillTokenInfoAtCursor(context);

            return SignatureProvider.getSignatureHelp(context);
        });
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalRenameContext context = ContextBuilder.renameContext(this.serverContext, params);
            return RenameProvider.getRename(context);
        });
    }

    @Override
    public CompletableFuture<Either<Range, PrepareRenameResult>> prepareRename(PrepareRenameParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalPrepareRenameContext context = ContextBuilder.prepareRenameContext(this.serverContext, params);
            ContextEvaluator.fillTokenInfoAtCursor(context);
            PrepareRenameResult renameResult = RenameProvider.prepareRename(context);
            if (renameResult == null) {
                return null;
            }

            return Either.forRight(renameResult);
        });
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
        BaseOperationContext context = ContextBuilder.baseContext(this.serverContext);
        return CompletableFuture.supplyAsync(() -> FormatProvider.format(context, params));
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
        BaseOperationContext context = ContextBuilder.baseContext(this.serverContext);
        return CompletableFuture.supplyAsync(() -> FormatProvider.formatRange(context, params));
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
        String uri = params.getTextDocument().getUri();
        Position position = params.getPosition();
        BalPosBasedContext context = ContextBuilder.getPosBasedContext(this.serverContext, uri, position);
        ContextEvaluator.fillTokenInfoAtCursor(context);
        return CompletableFuture.supplyAsync(() -> FormatProvider.onTypeFormat(context, params));
    }

    @Override
    public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalCodeActionContext context = ContextBuilder.getCodeActionContext(this.serverContext, params);
            ContextEvaluator.fillTokenInfoAtCursor(context);
            return CodeActionProvider.getCodeAction(context, params);
        });
    }

    @Override
    public CompletableFuture<CodeAction> resolveCodeAction(CodeAction unresolved) {
        return CompletableFuture.supplyAsync(() -> {
            String uri = ((JsonObject) unresolved.getData()).get("uri").getAsString();
            BalTextDocumentContext context = ContextBuilder.getTextDocumentContext(this.serverContext, uri);
            return CodeActionProvider.resolve(context, unresolved);
        });
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalCodeLensContext context = ContextBuilder.getCodeLensContext(this.serverContext, params);
            return CodeLensProvider.getCodeLenses(context, params);
        });
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
        BalReferencesContext context = ContextBuilder.getReferencesContext(this.serverContext, params);

        return CompletableFuture.supplyAsync(() -> ReferencesProvider.references(context));
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>>
    definition(DefinitionParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalDefinitionContext context = ContextBuilder.getDefinitionContext(this.serverContext, params);
            ContextEvaluator.fillTokenInfoAtCursor(context);
//            if (this.serverContext.getClientCapabilities().get().getTextDocument().getDefinition().getLinkSupport()) {
//                List<LocationLink> locationLinks = DefinitionProvider.definitionWithLocationLink(context);
//                return Either.forRight(locationLinks);
//            }
            List<Location> definitions = DefinitionProvider.definition(context);
            return Either.forLeft(definitions);
        });
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> typeDefinition(TypeDefinitionParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalTypeDefContext context = ContextBuilder.getTypeDefinitionContext(this.serverContext, params);
            ContextEvaluator.fillTokenInfoAtCursor(context);
            List<Location> definitions = DefinitionProvider.typeDefinition(context);
            return Either.forLeft(definitions);
        });
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> implementation(ImplementationParams params) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                BalGotoImplContext context = ContextBuilder.getGotoImplContext(this.serverContext, params);
                ContextEvaluator.fillTokenInfoAtCursor(context);
                List<Location> definitions = DefinitionProvider.implementation(context);
                return Either.forLeft(definitions);
            } catch (Throwable e) {
                return null;
            }
        });
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>>
    declaration(DeclarationParams params) {
        return CompletableFuture.supplyAsync(() -> {
           try {
               BalDeclarationContext context = ContextBuilder.getDeclarationContext(this.serverContext, params);
               ContextEvaluator.fillTokenInfoAtCursor(context);
               List<Location> declaration = DefinitionProvider.declaration(context);
               
               return Either.forLeft(declaration);
           } catch (Throwable e) {
               return null;
           }
        });
    }

    @Override
    public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>>
    documentSymbol(DocumentSymbolParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalDocumentSymbolContext context = ContextBuilder.documentSymbolContext(this.serverContext, params);
            // return DocumentSymbolProvider.getDocumentSymbol(context);
            return DocumentSymbolProvider.getSymbolInformation(context);
        });
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(DocumentHighlightParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalDocumentHighlightContext context = ContextBuilder.documentHighlightContext(this.serverContext, params);
            return DocumentHighlightProvider.getHighlight(context);
        });
    }

    @Override
    public CompletableFuture<List<DocumentLink>> documentLink(DocumentLinkParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalDocumentLinkContext context = ContextBuilder.documentLinkContext(this.serverContext, params);
            return DocumentLinkProvider.getDocumentLink(context);
        });
    }

    @Override
    public CompletableFuture<DocumentLink> documentLinkResolve(DocumentLink documentLink) {
        return CompletableFuture.supplyAsync(() -> {
            BaseOperationContext context = ContextBuilder.baseContext(this.serverContext);
            return DocumentLinkProvider.getDocumentLinkResolved(context, documentLink);
        });
    }

    @Override
    public CompletableFuture<List<SelectionRange>> selectionRange(SelectionRangeParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalSelectionRangeContext context = ContextBuilder.getSelectionRangeContext(this.serverContext, params);
            return SelectionRangeProvider.getSelectionRange(context);
        });
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalSemanticTokenContext context = ContextBuilder.semanticTokensContext(this.serverContext, params);

            return SemanticTokensProvider.getSemanticTokens(context);
        });
    }

    @Override
    public CompletableFuture<Either<SemanticTokens, SemanticTokensDelta>> semanticTokensFullDelta(SemanticTokensDeltaParams params) {
        return null;
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensRange(SemanticTokensRangeParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalSemanticTokenRangeContext context = ContextBuilder.semanticTokensRangeContext(this.serverContext, params);

            return SemanticTokensProvider.getSemanticTokensInRange(context);
        });
    }

    @Override
    public CompletableFuture<LinkedEditingRanges> linkedEditingRange(LinkedEditingRangeParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalLinkedEditingRangeContext context = ContextBuilder.getLinkedEditingRangeContext(this.serverContext, params);
            ContextEvaluator.fillTokenInfoAtCursor(context);
            return LinkedEditingRangeProvider.getLinkedEditingRanges(context);
        });
    }

    @Override
    public CompletableFuture<List<ColorInformation>> documentColor(DocumentColorParams params) {
        BalDocumentColourContext context = ContextBuilder.getColourContext(this.serverContext, params);
        return CompletableFuture.supplyAsync(() -> DocumentColourProvider.getColours(context));
    }

    @Override
    public CompletableFuture<List<ColorPresentation>> colorPresentation(ColorPresentationParams params) {
        return CompletableFuture.supplyAsync(() -> DocumentColourProvider.getColourPresentation(params));
    }

    @Override
    public CompletableFuture<List<FoldingRange>> foldingRange(FoldingRangeRequestParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalFoldingRangeContext context = ContextBuilder.getFoldingRangeContext(this.serverContext, params);
            return FoldingRangeProvider.getFoldingRanges(context);
        });
    }

    @Override
    public CompletableFuture<List<CallHierarchyItem>> prepareCallHierarchy(CallHierarchyPrepareParams params) {
        return CompletableFuture.supplyAsync(() -> {
            BalPosBasedContext context = ContextBuilder.getPosBasedContext(this.serverContext,
                    params.getTextDocument().getUri(), params.getPosition());
            return CallHierarchyProvider.prepare(context);
        });
    }

    @Override
    public CompletableFuture<List<CallHierarchyIncomingCall>> callHierarchyIncomingCalls(CallHierarchyIncomingCallsParams params) {
        return CompletableFuture.supplyAsync(() -> {
            CallHierarchyItem item = params.getItem();
            BalPosBasedContext context = ContextBuilder.getPosBasedContext(this.serverContext,
                    item.getUri(), item.getRange().getStart());

            return CallHierarchyProvider.incoming(context);
        });
    }

    @Override
    public CompletableFuture<List<CallHierarchyOutgoingCall>> callHierarchyOutgoingCalls(CallHierarchyOutgoingCallsParams params) {
        return CompletableFuture.supplyAsync(() -> {
            CallHierarchyItem item = params.getItem();
            BalCallHierarchyOutgoingContext context = ContextBuilder.getCallHierarchyOutGoingContext(this.serverContext,
                    item);

            return CallHierarchyProvider.outgoing(context);
        });
    }
}
