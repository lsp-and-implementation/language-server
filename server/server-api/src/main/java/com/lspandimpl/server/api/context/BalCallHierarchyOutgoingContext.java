package com.lspandimpl.server.api.context;

import io.ballerina.compiler.syntax.tree.NonTerminalNode;

public interface BalCallHierarchyOutgoingContext extends BalTextDocumentContext {
    NonTerminalNode nodeForItem();
}
