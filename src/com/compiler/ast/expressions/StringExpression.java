package com.compiler.ast.expressions;

import com.compiler.ast.Expression;

public class StringExpression extends Expression {
    public final String value;

    public StringExpression(String value) {
        this.value = value;
    }
}
