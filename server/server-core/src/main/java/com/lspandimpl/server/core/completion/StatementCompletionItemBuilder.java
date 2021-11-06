package com.lspandimpl.server.core.completion;

import com.lspandimpl.server.core.utils.CommonUtils;

public class StatementCompletionItemBuilder {
    private StatementCompletionItemBuilder() {
    }
    
    public static StatementBlock getIfStatement() {
        String plainText = "if true {" + CommonUtils.LINE_SEPARATOR + "\t" + CommonUtils.LINE_SEPARATOR + "}";
        String snippet = "if ${1:true} {" + CommonUtils.LINE_SEPARATOR + "\t${2}" + CommonUtils.LINE_SEPARATOR + "}";
        
        return new StatementBlock(plainText, snippet, "if");
    }
    
    public static StatementBlock getWhileStatement() {
        String plainText = "while true {" + CommonUtils.LINE_SEPARATOR + "\t" + CommonUtils.LINE_SEPARATOR + "}";
        String snippet = "while ${1:true} {" + CommonUtils.LINE_SEPARATOR + "\t${2}" + CommonUtils.LINE_SEPARATOR + "}";
        
        return new StatementBlock(plainText, snippet, "while");
    }
    
    public static class StatementBlock {
        private final String plainText;
        private final String snippet;
        private final String label;

        public StatementBlock(String plainText, String snippet, String label) {
            this.plainText = plainText;
            this.snippet = snippet;
            this.label = label;
        }

        public String getPlainText() {
            return plainText;
        }

        public String getSnippet() {
            return snippet;
        }

        public String getLabel() {
            return label;
        }
    }
}
