package com.compiler.ast.expressions;

import com.compiler.ast.Expression;

public class NumberExpression extends Expression {
    public double value;

    public NumberExpression(double value) {
        this.value = value;
    }
}