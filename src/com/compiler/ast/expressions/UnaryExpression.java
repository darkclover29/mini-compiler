package com.compiler.ast.expressions;

import com.compiler.ast.Expression;

public class UnaryExpression extends Expression {
    public final String operator;
    public final Expression expression;

    public UnaryExpression(String operator, Expression expression) {
        this.operator = operator;
        this.expression = expression;
    }
}
