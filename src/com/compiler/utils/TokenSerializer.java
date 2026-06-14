package com.compiler.utils;

import com.compiler.lexer.Token;
import java.util.List;

public class TokenSerializer {

    public static String toJson(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            sb.append(String.format("{\"type\":\"%s\",\"value\":\"%s\"}", 
                t.type.name(), escape(t.value)));
            if (i < tokens.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
