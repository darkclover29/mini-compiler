package com.compiler.ast.expressions;

import com.compiler.ast.Expression;

public class IndexExpression extends Expression {
    public final Expression target;
    public final Expression index;

    public IndexExpression(Expression target, Expression index) {
        this.target = target;
        this.index = index;
    }
}
