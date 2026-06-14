package com.compiler.ast.statements;

import com.compiler.ast.Statement;
import com.compiler.ast.Expression;

public class Assignment extends Statement {
    public String name;
    public Expression value;

    public Assignment(String name, Expression value) {
        this.name = name;
        this.value = value;
    }
}