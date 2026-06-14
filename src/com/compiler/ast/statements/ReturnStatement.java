package com.compiler.ast.statements;

import com.compiler.ast.Expression;
import com.compiler.ast.Statement;

public class ReturnStatement extends Statement {
    public final Expression expression;

    public ReturnStatement(Expression expression) {
        this.expression = expression;
    }
}
