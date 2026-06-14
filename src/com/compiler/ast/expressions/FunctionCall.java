package com.compiler.ast.expressions;

import com.compiler.ast.Expression;
import java.util.List;

public class FunctionCall extends Expression {
    public final String name;
    public final List<Expression> arguments;

    public FunctionCall(String name, List<Expression> arguments) {
        this.name = name;
        this.arguments = arguments;
    }
}
