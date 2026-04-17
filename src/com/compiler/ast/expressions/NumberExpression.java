package com.compiler.ast.expressions;

import com.compiler.ast.Expression;

public class NumberExpression extends Expression {
    public int value;

    public NumberExpression(int value) {
        this.value = value;
    }
}