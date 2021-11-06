package com.lspandimpl.server.core.callhierarchy;

import com.lspandimpl.server.api.context.BalCallHierarchyOutgoingContext;
import com.lspandimpl.server.api.context.BalTextDocumentContext;
import com.lspandimpl.server.ballerina.compiler.workspace.CompilerManager;
import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Document;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.CallHierarchyIncomingCall;
import org.eclipse.lsp4j.CallHierarchyItem;
import org.eclipse.lsp4j.CallHierarchyOutgoingCall;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import com.lspandimpl.server.api.context.BalPosBasedContext;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CallHierarchyProvider {
    private CallHierarchyProvider() {
    }

    public static List<CallHierarchyItem> prepare(
            BalPosBasedContext context) {
        CompilerManager compilerManager = context.compilerManager();
        Path path = context.getPath();
        Document document = context.currentDocument().orElseThrow();
        Position position = context.getCursorPosition();
        SemanticModel semanticModel = compilerManager.getSemanticModel(path).orElseThrow();
        LinePosition linePos = LinePosition.from(position.getLine(), position.getCharacter());
        Optional<Symbol> symbol = semanticModel.symbol(document, linePos);
        if (symbol.isEmpty() || symbol.get().kind() != SymbolKind.FUNCTION) {
            return Collections.emptyList();
        }
        FunctionSymbol functionSymbol = (FunctionSymbol) symbol.get();
        CallHierarchyItem cItem = new CallHierarchyItem();
        cItem.setKind(org.eclipse.lsp4j.SymbolKind.Function);
        cItem.setName(functionSymbol.getName().orElseThrow());

        LineRange lineRange = functionSymbol.getLocation().get().lineRange();
        Position rStart = new Position(lineRange.startLine().line(), lineRange.startLine().offset());
        Position rEnd = new Position(lineRange.endLine().line(), lineRange.endLine().offset());
        cItem.setRange(new Range(rStart, rEnd));

        Optional<Node> funcNode = compilerManager.getNode(path, lineRange.startLine().line(),
                lineRange.startLine().offset());
        if (funcNode.isEmpty() || funcNode.get().kind() != SyntaxKind.FUNCTION_DEFINITION) {
            return Collections.emptyList();
        }
        IdentifierToken fName = ((FunctionDefinitionNode) funcNode.get()).functionName();
        LineRange nameLineRange = fName.lineRange();
        Position srStart = new Position(nameLineRange.startLine().line(), nameLineRange.startLine().offset());
        Position srEnd = new Position(nameLineRange.endLine().line(), nameLineRange.endLine().offset());
        Path prjRoot = compilerManager.getProjectRoot(path).orElseThrow();
        String uri = getUriFromLocation(symbol.get(), symbol.get().getLocation().get(), prjRoot);
        cItem.setSelectionRange(new Range(srStart, srEnd));
        cItem.setUri(uri);
        /*
        Necessary semantic information can be added with data field as
        cItem.setData();
         */

        return Collections.singletonList(cItem);
    }

    public static List<CallHierarchyIncomingCall> incoming(BalPosBasedContext context) {
        CompilerManager compilerManager = context.compilerManager();
        Path path = context.getPath();
        Document document = context.currentDocument().orElseThrow();
        Position position = context.getCursorPosition();
        SemanticModel semanticModel = compilerManager.getSemanticModel(path).orElseThrow();
        Optional<Symbol> symbol = semanticModel.symbol(document, LinePosition.from(position.getLine(), position.getCharacter()));
        if (symbol.isEmpty() || symbol.get().kind() != SymbolKind.FUNCTION) {
            return Collections.emptyList();
        }
        List<CallHierarchyIncomingCall> calls = new ArrayList<>();
        Map<FunctionDefinitionNode, List<Range>> callsMap =
                getIncomingCallsMap(semanticModel, symbol.get(), context);

        Path prjRoot = compilerManager.getProjectRoot(context.getPath()).orElseThrow();
        callsMap.forEach((fDef, ranges) -> {
            CallHierarchyItem cItem = new CallHierarchyItem();
            cItem.setKind(org.eclipse.lsp4j.SymbolKind.Function);
            cItem.setName(fDef.functionName().text());

            LineRange lineRange = fDef.lineRange();
            Position rStart = new Position(lineRange.startLine().line(), lineRange.startLine().offset());
            Position rEnd = new Position(lineRange.endLine().line(), lineRange.endLine().offset());
            cItem.setRange(new Range(rStart, rEnd));

            LineRange nameLineRange = fDef.functionName().lineRange();
            Position srStart = new Position(nameLineRange.startLine().line(), nameLineRange.startLine().offset());
            Position srEnd = new Position(nameLineRange.endLine().line(), nameLineRange.endLine().offset());
            cItem.setSelectionRange(new Range(srStart, srEnd));

            Symbol fDefSymbol = semanticModel.symbol(fDef).orElseThrow();
            String uri = getUriFromLocation(fDefSymbol, fDefSymbol.getLocation().orElseThrow(), prjRoot);

            cItem.setUri(uri);

            CallHierarchyIncomingCall call = new CallHierarchyIncomingCall();
            call.setFrom(cItem);
            call.setFromRanges(ranges);

            calls.add(call);
        });

        return calls;
    }

    public static List<CallHierarchyOutgoingCall> outgoing(BalCallHierarchyOutgoingContext context) {
        NonTerminalNode nodeAtCursor = context.nodeForItem();
        if (nodeAtCursor.kind() != SyntaxKind.FUNCTION_DEFINITION) {
            return Collections.emptyList();
        }
        OutgoingCallVisitor visitor = new OutgoingCallVisitor();
        nodeAtCursor.accept(visitor);
        List<FunctionCallExpressionNode> outgoingCalls = visitor.getOutgoingCalls();
        CompilerManager compilerManager = context.compilerManager();
        Path prjRoot = compilerManager.getProjectRoot(context.getPath()).get();

        Map<Symbol, List<Range>> outgoingCallsMap = getOutgoingCallsMap(outgoingCalls, context);
        List<CallHierarchyOutgoingCall> calls = new ArrayList<>();
        outgoingCallsMap.forEach((symbol, ranges) -> {
            LineRange nameLineRange = symbol.getLocation().get().lineRange();
            LinePosition startLine = nameLineRange.startLine();
            Node node = compilerManager.getNode(context.getPath(), startLine.line(), startLine.offset()).get();

            LineRange lineRange = node.lineRange();
            Position rStart = new Position(lineRange.startLine().line(), lineRange.startLine().offset());
            Position rEnd = new Position(lineRange.endLine().line(), lineRange.endLine().offset());

            Position srStart = new Position(startLine.line(), startLine.offset());
            Position srEnd = new Position(nameLineRange.endLine().line(), nameLineRange.endLine().offset());
            String uri = getUriFromLocation(symbol, symbol.getLocation().orElseThrow(), prjRoot);

            CallHierarchyItem cItem = new CallHierarchyItem();
            cItem.setKind(org.eclipse.lsp4j.SymbolKind.Function);
            cItem.setName(symbol.getName().get());
            cItem.setRange(new Range(rStart, rEnd));
            cItem.setSelectionRange(new Range(srStart, srEnd));
            cItem.setUri(uri);

            CallHierarchyOutgoingCall call = new CallHierarchyOutgoingCall();
            call.setTo(cItem);
            call.setFromRanges(ranges);
            calls.add(call);
        });

        return calls;
    }

    private static Map<Symbol, List<Range>> getOutgoingCallsMap(List<FunctionCallExpressionNode> calls,
                                                                BalTextDocumentContext context) {
        Map<Symbol, List<Range>> symbolMap = new HashMap<>();

        SemanticModel semanticModel = context.compilerManager().getSemanticModel(context.getPath()).orElseThrow();
        for (FunctionCallExpressionNode outgoingCall : calls) {
            Optional<Symbol> symbol = semanticModel.symbol(outgoingCall);
            if (symbol.isEmpty() || symbol.get().getLocation().isEmpty()) {
                continue;
            }
            Location location = symbol.get().getLocation().get();
            LinePosition startLine = location.lineRange().startLine();
            LinePosition endLine = location.lineRange().endLine();
            Position start = new Position(startLine.line(), startLine.offset());
            Position end = new Position(endLine.line(), endLine.offset());
            Range range = new Range(start, end);

            if (symbolMap.containsKey(symbol.get())) {
                symbolMap.get(symbol.get()).add(range);
            } else {
                List<Range> ranges = new ArrayList<>();
                ranges.add(range);
                symbolMap.put(symbol.get(), ranges);
            }
        }

        return symbolMap;
    }

    private static Map<FunctionDefinitionNode, List<Range>> getIncomingCallsMap(SemanticModel semanticModel,
                                                                                Symbol symbol,
                                                                                BalPosBasedContext context) {
        Map<FunctionDefinitionNode, List<Range>> callMap = new HashMap<>();
        List<Location> references = semanticModel.references(symbol, false);

        for (Location reference : references) {
            Optional<FunctionDefinitionNode> enclosedFunction = getEnclosedFunction(context, reference);
            if (enclosedFunction.isEmpty()) {
                continue;
            }
            LineRange refRange = reference.lineRange();
            Position srStart = new Position(refRange.startLine().line(), refRange.startLine().offset());
            Position srEnd = new Position(refRange.endLine().line(), refRange.endLine().offset());
            Range range = new Range(srStart, srEnd);

            if (callMap.containsKey(enclosedFunction.get())) {
                callMap.get(enclosedFunction.get()).add(range);
            } else {
                List<Range> ranges = new ArrayList<>();
                ranges.add(range);
                callMap.put(enclosedFunction.get(), ranges);
            }
        }

        return callMap;
    }

    private static Optional<FunctionDefinitionNode> getEnclosedFunction(BalPosBasedContext context, Location location) {
        Path path = context.getPath();
        LinePosition startLine = location.lineRange().startLine();
        Optional<Node> node = context.compilerManager().getNode(path, startLine.line(), startLine.offset());
        if (node.isEmpty()) {
            return Optional.empty();
        }
        Node evalNode = node.get();
        while (evalNode.kind() != SyntaxKind.MODULE_PART
                && evalNode.kind() != SyntaxKind.FUNCTION_DEFINITION) {
            evalNode = evalNode.parent();
        }

        return evalNode.kind() == SyntaxKind.FUNCTION_DEFINITION
                ? Optional.of((FunctionDefinitionNode) evalNode) : Optional.empty();
    }

    private static String getPath(Document document, BalPosBasedContext context) {
        Path path = context.getPath();
        Path projectRoot = context.compilerManager().getProjectRoot(path).orElseThrow();

        return projectRoot.resolve(document.name()).toUri().toString();
    }

    public static String getUriFromLocation(Symbol symbol, Location location, Path prjRoot) {
        ModuleID moduleID = symbol.getModule().orElseThrow().id();

        LineRange lineRange = location.lineRange();
        String filePath = lineRange.filePath();
        String[] moduleName = moduleID.moduleName().split(prjRoot.getFileName().toString() + "\\.");

        // TODO: Fix the following
//        if (module.project().kind() == ProjectKind.SINGLE_FILE_PROJECT) {
//            return prjRoot.toUri().toString();
//        } else if (module.isDefaultModule()) {
//            module.project();
//            return prjRoot.resolve(lineRange.filePath()).toUri().toString();
//        } else {
        return prjRoot.resolve("modules")
                .resolve(moduleName[moduleName.length - 1])
                .resolve(filePath).toUri().toString();
//        }
    }
}
