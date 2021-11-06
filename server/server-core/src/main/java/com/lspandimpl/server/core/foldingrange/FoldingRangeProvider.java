package com.lspandimpl.server.core.foldingrange;

import com.lspandimpl.server.api.context.BalFoldingRangeContext;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeKind;
import com.lspandimpl.server.ballerina.compiler.workspace.CompilerManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FoldingRangeProvider {
    private FoldingRangeProvider() {
    }
    
    public static List<FoldingRange> getFoldingRanges(BalFoldingRangeContext context) {
        List<FoldingRange> foldingRanges = new ArrayList<>();
        CompilerManager compilerManager = context.compilerManager();
        SyntaxTree syntaxTree = compilerManager.getSyntaxTree(context.getPath()).orElseThrow();
        
        ModulePartNode modulePartNode = syntaxTree.rootNode();

        NodeList<ImportDeclarationNode> imports = modulePartNode.imports();
        if (!imports.isEmpty() && imports.size() > 1) {
            ImportDeclarationNode firstImport = imports.get(0);
            ImportDeclarationNode lastImport = imports.get(imports.size() - 1);
            
            FoldingRange importsRange = new FoldingRange();
            importsRange.setStartLine(firstImport.lineRange().startLine().line());
            importsRange.setStartCharacter(firstImport.lineRange().startLine().offset());
            importsRange.setEndLine(lastImport.lineRange().endLine().line());
            importsRange.setStartCharacter(lastImport.lineRange().endLine().offset());
            importsRange.setKind(FoldingRangeKind.Imports);
            
            foldingRanges.add(importsRange);
        }

        for (ModuleMemberDeclarationNode member : modulePartNode.members()) {
            SyntaxKind memberKind = member.kind();
            if (memberKind != SyntaxKind.FUNCTION_DEFINITION) {
                continue;
            }
            FunctionDefinitionNode functionDef = (FunctionDefinitionNode) member;
            Token functionKeyword = functionDef.functionKeyword();
            FoldingRange functionRange = new FoldingRange();
            functionRange.setStartLine(functionKeyword.lineRange().startLine().line());
            functionRange.setStartCharacter(functionKeyword.lineRange().startLine().offset());
            functionRange.setEndLine(functionDef.lineRange().endLine().line());
            functionRange.setEndCharacter(functionDef.lineRange().endLine().offset());
            functionRange.setKind(FoldingRangeKind.Region);
            
            foldingRanges.add(functionRange);
            
            Optional<MetadataNode> metadata = functionDef.metadata();
            if (metadata.isEmpty()) {
                continue;
            }

            Optional<Node> docString = metadata.get().documentationString();
            if (docString.isPresent()) {
                FoldingRange docsRange = new FoldingRange();
                docsRange.setStartLine(docString.get().lineRange().startLine().line());
                docsRange.setStartCharacter(docString.get().lineRange().startLine().offset());
                docsRange.setEndLine(docString.get().lineRange().endLine().line());
                docsRange.setEndCharacter(docString.get().lineRange().endLine().offset());
                docsRange.setKind(FoldingRangeKind.Comment);
                
                foldingRanges.add(docsRange);
            }

            NodeList<AnnotationNode> annotations = metadata.get().annotations();
            if (annotations.isEmpty()) {
                continue;
            }
            AnnotationNode firstAnnot = annotations.get(0);
            AnnotationNode lastAnnot = annotations.get(annotations.size() - 1);

            FoldingRange annotRange = new FoldingRange();
            annotRange.setStartLine(firstAnnot.lineRange().startLine().line());
            annotRange.setStartCharacter(firstAnnot.lineRange().startLine().offset());
            annotRange.setEndLine(lastAnnot.lineRange().endLine().line());
            annotRange.setEndCharacter(lastAnnot.lineRange().endLine().offset());
            annotRange.setKind(FoldingRangeKind.Region);
            
            foldingRanges.add(annotRange);
        }
        
        return foldingRanges;
    }
}
