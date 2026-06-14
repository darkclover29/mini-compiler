package com.compiler.ast.statements;

import com.compiler.ast.Statement;
import com.compiler.ast.Expression;

public class PrintStatement extends Statement {
    public Expression expression;

    public PrintStatement(Expression expression) {
        this.expression = expression;
    }
}