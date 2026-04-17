package com.compiler.ast.expressions;

import com.compiler.ast.Expression;

public class VariableExpression extends Expression {
    public String name;

    public VariableExpression(String name) {
        this.name = name;
    }
}