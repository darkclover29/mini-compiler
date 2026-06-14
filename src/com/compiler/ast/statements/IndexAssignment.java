package com.compiler.ast.statements;

import com.compiler.ast.Statement;
import com.compiler.ast.expressions.IndexExpression;
import com.compiler.ast.Expression;

public class IndexAssignment extends Statement {
    public final IndexExpression target;
    public final Expression value;

    public IndexAssignment(IndexExpression target, Expression value) {
        this.target = target;
        this.value = value;
    }
}
