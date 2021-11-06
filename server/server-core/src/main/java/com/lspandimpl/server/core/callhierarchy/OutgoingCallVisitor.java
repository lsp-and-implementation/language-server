package com.lspandimpl.server.core.callhierarchy;

import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;

import java.util.ArrayList;
import java.util.List;

public class OutgoingCallVisitor extends NodeVisitor {
    private final List<FunctionCallExpressionNode> outgoingCalls = new ArrayList<>();
    
    @Override
    public void visit(FunctionCallExpressionNode node) {
        this.outgoingCalls.add(node);
//        node.accept(this);
    }

    public List<FunctionCallExpressionNode> getOutgoingCalls() {
        return outgoingCalls;
    }
}
