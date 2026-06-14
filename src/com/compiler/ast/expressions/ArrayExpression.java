package com.compiler.ast.expressions;

import com.compiler.ast.Expression;
import java.util.List;

public class ArrayExpression extends Expression {
    public final List<Expression> elements;

    public ArrayExpression(List<Expression> elements) {
        this.elements = elements;
    }
}
