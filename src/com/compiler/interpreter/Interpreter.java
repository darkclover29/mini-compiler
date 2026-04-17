package com.compiler.interpreter;

import com.compiler.ast.*;
import com.compiler.ast.statements.*;
import com.compiler.ast.expressions.*;

import java.util.List;

public class Interpreter {

    private Environment env = new Environment();

    public void execute(List<Statement> statements) {
        for (Statement stmt : statements) {
            executeStatement(stmt);
        }
    }

    private void executeStatement(Statement stmt) {

        if (stmt instanceof PrintStatement) {
            int value = evaluate(((PrintStatement) stmt).expression);
            System.out.println(value);
        }

        else if (stmt instanceof VarDeclaration) {
            VarDeclaration vd = (VarDeclaration) stmt;
            env.set(vd.name, evaluate(vd.value));
        }

        else if (stmt instanceof Assignment) {
            Assignment as = (Assignment) stmt;
            env.set(as.name, evaluate(as.value));
        }

        else if (stmt instanceof IfStatement) {
            IfStatement ifs = (IfStatement) stmt;

            if (evaluate(ifs.condition) != 0) {
                execute(ifs.thenBranch);
            } else {
                execute(ifs.elseBranch);
            }
        }

        else if (stmt instanceof WhileStatement) {
            WhileStatement ws = (WhileStatement) stmt;

            while (evaluate(ws.condition) != 0) {
                execute(ws.body);
            }
        }
    }

    private int evaluate(Expression expr) {

        if (expr instanceof NumberExpression) {
            return ((NumberExpression) expr).value;
        }

        if (expr instanceof VariableExpression) {
            return env.get(((VariableExpression) expr).name);
        }

        if (expr instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression) expr;

            int left = evaluate(be.left);
            int right = evaluate(be.right);

            switch (be.operator) {
                case "+": return left + right;
                case "-": return left - right;
                case "*": return left * right;
                case "/": return left / right;
                case "<": return left < right ? 1 : 0;
                case ">": return left > right ? 1 : 0;
                case "==": return left == right ? 1 : 0;
            }
        }

        throw new RuntimeException("Invalid Expression");
    }
}