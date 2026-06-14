package com.compiler.utils;

import com.compiler.ast.Expression;
import com.compiler.ast.Statement;
import com.compiler.ast.expressions.*;
import com.compiler.ast.statements.*;

import java.util.List;

public class AstSerializer {

    public static String toJson(List<Statement> statements) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < statements.size(); i++) {
            sb.append(statementToJson(statements.get(i)));
            if (i < statements.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String statementToJson(Statement stmt) {
        if (stmt == null) return "null";

        if (stmt instanceof PrintStatement) {
            return String.format("{\"type\":\"PrintStatement\",\"expression\":%s}", 
                expressionToJson(((PrintStatement) stmt).expression));
        }
        if (stmt instanceof VarDeclaration) {
            VarDeclaration vd = (VarDeclaration) stmt;
            return String.format("{\"type\":\"VarDeclaration\",\"name\":\"%s\",\"value\":%s}", 
                escape(vd.name), expressionToJson(vd.value));
        }
        if (stmt instanceof Assignment) {
            Assignment as = (Assignment) stmt;
            return String.format("{\"type\":\"Assignment\",\"name\":\"%s\",\"value\":%s}", 
                escape(as.name), expressionToJson(as.value));
        }
        if (stmt instanceof IfStatement) {
            IfStatement ifs = (IfStatement) stmt;
            return String.format("{\"type\":\"IfStatement\",\"condition\":%s,\"thenBranch\":%s,\"elseBranch\":%s}", 
                expressionToJson(ifs.condition), toJson(ifs.thenBranch), toJson(ifs.elseBranch));
        }
        if (stmt instanceof WhileStatement) {
            WhileStatement ws = (WhileStatement) stmt;
            return String.format("{\"type\":\"WhileStatement\",\"condition\":%s,\"body\":%s}", 
                expressionToJson(ws.condition), toJson(ws.body));
        }
        if (stmt instanceof FunctionDeclaration) {
            FunctionDeclaration fd = (FunctionDeclaration) stmt;
            StringBuilder params = new StringBuilder("[");
            for (int i = 0; i < fd.parameters.size(); i++) {
                params.append("\"").append(escape(fd.parameters.get(i))).append("\"");
                if (i < fd.parameters.size() - 1) {
                    params.append(",");
                }
            }
            params.append("]");
            return String.format("{\"type\":\"FunctionDeclaration\",\"name\":\"%s\",\"parameters\":%s,\"body\":%s}", 
                escape(fd.name), params.toString(), toJson(fd.body));
        }
        if (stmt instanceof ReturnStatement) {
            ReturnStatement rs = (ReturnStatement) stmt;
            return String.format("{\"type\":\"ReturnStatement\",\"expression\":%s}", 
                expressionToJson(rs.expression));
        }
        if (stmt instanceof CallStatement) {
            CallStatement cs = (CallStatement) stmt;
            return String.format("{\"type\":\"CallStatement\",\"call\":%s}", 
                expressionToJson(cs.call));
        }
        if (stmt instanceof BlockStatement) {
            BlockStatement bs = (BlockStatement) stmt;
            return String.format("{\"type\":\"BlockStatement\",\"statements\":%s}", 
                toJson(bs.statements));
        }
        if (stmt instanceof IndexAssignment) {
            IndexAssignment ia = (IndexAssignment) stmt;
            return String.format("{\"type\":\"IndexAssignment\",\"target\":%s,\"value\":%s}", 
                expressionToJson(ia.target), expressionToJson(ia.value));
        }
        return "{\"type\":\"UnknownStatement\"}";
    }

    private static String expressionToJson(Expression expr) {
        if (expr == null) return "null";

        if (expr instanceof NumberExpression) {
            return String.format("{\"type\":\"NumberExpression\",\"value\":%s}", 
                ((NumberExpression) expr).value);
        }
        if (expr instanceof VariableExpression) {
            return String.format("{\"type\":\"VariableExpression\",\"name\":\"%s\"}", 
                escape(((VariableExpression) expr).name));
        }
        if (expr instanceof StringExpression) {
            return String.format("{\"type\":\"StringExpression\",\"value\":\"%s\"}", 
                escape(((StringExpression) expr).value));
        }
        if (expr instanceof ArrayExpression) {
            ArrayExpression ae = (ArrayExpression) expr;
            StringBuilder elems = new StringBuilder("[");
            for (int i = 0; i < ae.elements.size(); i++) {
                elems.append(expressionToJson(ae.elements.get(i)));
                if (i < ae.elements.size() - 1) {
                    elems.append(",");
                }
            }
            elems.append("]");
            return String.format("{\"type\":\"ArrayExpression\",\"elements\":%s}", elems.toString());
        }
        if (expr instanceof IndexExpression) {
            IndexExpression ie = (IndexExpression) expr;
            return String.format("{\"type\":\"IndexExpression\",\"target\":%s,\"index\":%s}", 
                expressionToJson(ie.target), expressionToJson(ie.index));
        }
        if (expr instanceof UnaryExpression) {
            UnaryExpression ue = (UnaryExpression) expr;
            return String.format("{\"type\":\"UnaryExpression\",\"operator\":\"%s\",\"expression\":%s}", 
                escape(ue.operator), expressionToJson(ue.expression));
        }
        if (expr instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) expr;
            StringBuilder args = new StringBuilder("[");
            for (int i = 0; i < fc.arguments.size(); i++) {
                args.append(expressionToJson(fc.arguments.get(i)));
                if (i < fc.arguments.size() - 1) {
                    args.append(",");
                }
            }
            args.append("]");
            return String.format("{\"type\":\"FunctionCall\",\"name\":\"%s\",\"arguments\":%s}", 
                escape(fc.name), args.toString());
        }
        if (expr instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression) expr;
            return String.format("{\"type\":\"BinaryExpression\",\"left\":%s,\"operator\":\"%s\",\"right\":%s}", 
                expressionToJson(be.left), escape(be.operator), expressionToJson(be.right));
        }
        return "{\"type\":\"UnknownExpression\"}";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
