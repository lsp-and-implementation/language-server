package com.lspandimpl.server.core.signature;

import com.lspandimpl.server.api.context.BalSignatureContext;
import com.lspandimpl.server.core.utils.CommonUtils;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.ParameterInformation;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpCapabilities;
import org.eclipse.lsp4j.SignatureInformation;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Tuple;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SignatureProvider {
    public static SignatureHelp
    getSignatureHelp(BalSignatureContext context) {
        Optional<FunctionCallExpressionNode> fexpr =
                getFunctionCallExpr(context);
        if (fexpr.isEmpty()) {
            return null;
        }
        int activeParameter = getActiveParameter(context, fexpr.get());
        boolean contextSupport = context.clientCapabilities()
                .getTextDocument().getSignatureHelp()
                .getContextSupport();
        boolean retrigger = context.getParams().getContext().isRetrigger();
        // If retrigger, we return the same 
        // signature by changing active parameter
        if (contextSupport && retrigger) {
            SignatureHelp activeSignatureHelp = context.getParams()
                    .getContext().getActiveSignatureHelp();
            List<SignatureInformation> signatures =
                    activeSignatureHelp.getSignatures();
            for (SignatureInformation signature : signatures) {
                signature.setActiveParameter(activeParameter);
            }

            return activeSignatureHelp;
        }

        Optional<SignatureInformation> signatureInformation =
                getSignatureInformation(context, fexpr.get(), activeParameter);
        if (signatureInformation.isEmpty()) {
            return null;
        }

        SignatureHelp signatureHelp = new SignatureHelp();
        // Since Ballerina does not have method overriding
        signatureHelp.setActiveSignature(0);
        signatureHelp.setSignatures(Collections
                .singletonList(signatureInformation.get()));

        return signatureHelp;
    }

    private static int getActiveParameter(BalSignatureContext context, FunctionCallExpressionNode node) {
        SeparatedNodeList<FunctionArgumentNode> arguments = node.arguments();
        int separators = arguments.separatorSize();

        if (separators == 0) {
            return 0;
        }

        for (int i = separators - 1; i >= 0; i--) {
            Token separator = arguments.getSeparator(i);
            int cursor = context.getCursorPositionInTree();
            if (cursor >= separator.textRange().endOffset()) {
                return i + 1;
            }
        }

        return -1;
    }

    private static Optional<SignatureInformation> getSignatureInformation(BalSignatureContext context,
                                                                          FunctionCallExpressionNode node,
                                                                          int activeParameter) {
        String functionName = ((SimpleNameReferenceNode) node.functionName()).name().text();
        Optional<FunctionSymbol> functionSymbol = context.visibleSymbols().stream()
                .filter(symbol -> symbol.kind() == SymbolKind.FUNCTION
                        && symbol.getName().orElse("").equals(functionName))
                .map(symbol -> (FunctionSymbol) symbol)
                .findFirst();

        if (functionSymbol.isEmpty()) {
            return Optional.empty();
        }

        SignatureHelpCapabilities capabilities = context.clientCapabilities().getTextDocument().getSignatureHelp();
        List<String> docFormat = capabilities.getSignatureInformation().getDocumentationFormat();
        Documentation documentation = functionSymbol.get().documentation().orElseThrow();
        String description = documentation.description().isPresent() ? documentation.description().get() : "";

        StringBuilder signatureLabel = new StringBuilder(functionSymbol.get().getName().get());
        signatureLabel.append("(");

        List<ParameterSymbol> parameters =
                functionSymbol.get().typeDescriptor().params().orElse(Collections.emptyList());
        int paramOffsetCounter = signatureLabel.toString().length();
        List<ParameterInformation> paramInfos = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            ParameterSymbol param = parameters.get(i);
            ParameterInformation paramInfo = new ParameterInformation();
            int start = paramOffsetCounter;
            signatureLabel.append(param.signature());
            paramOffsetCounter = signatureLabel.toString().length();
            int end = paramOffsetCounter;
            Tuple.Two<Integer, Integer> tuple = new Tuple.Two<>(start, end);
            paramInfo.setLabel(tuple);
            if (i < parameters.size() - 1) {
                signatureLabel.append(", ");
            }

            paramInfos.add(paramInfo);
        }
        signatureLabel.append(")");

        Either<String, MarkupContent> signatureDocs;
        if (docFormat.contains(MarkupKind.MARKDOWN)) {
            MarkupContent markupContent = new MarkupContent();
            markupContent.setKind(MarkupKind.MARKDOWN);
            markupContent.setValue("## Description " + CommonUtils.MD_LINE_SEPARATOR + description);

            signatureDocs = Either.forRight(markupContent);
        } else {
            signatureDocs = Either.forLeft(description);
        }

        SignatureInformation signatureInformation = new SignatureInformation();
        signatureInformation.setDocumentation(signatureDocs);
        signatureInformation.setLabel(signatureLabel.toString());
        signatureInformation.setParameters(paramInfos);
        signatureInformation.setActiveParameter(activeParameter);

        return Optional.of(signatureInformation);
    }

    private static Optional<FunctionCallExpressionNode> getFunctionCallExpr(BalSignatureContext context) {
        Path path = context.getPath();
        Position position = context.getParams().getPosition();
        Optional<Node> nodeAtCursor = context.compilerManager()
                .getNode(path, position.getLine(), position.getCharacter());

        if (nodeAtCursor.isEmpty()) {
            return Optional.empty();
        }

        Node functionCall = nodeAtCursor.get();
        while (functionCall != null) {
            if (functionCall.kind() == SyntaxKind.FUNCTION_CALL) {
                return Optional.of((FunctionCallExpressionNode) functionCall);
            }
            functionCall = functionCall.parent();
        }

        return Optional.empty();
    }
}
