package com.compiler.ast.statements;

import com.compiler.ast.Statement;
import com.compiler.ast.Expression;

public class VarDeclaration extends Statement {
    public String name;
    public Expression value;

    public VarDeclaration(String name, Expression value) {
        this.name = name;
        this.value = value;
    }
}