package com.compiler.ast.statements;

import com.compiler.ast.Statement;
import java.util.List;

public class FunctionDeclaration extends Statement {
    public final String name;
    public final List<String> parameters;
    public final List<Statement> body;

    public FunctionDeclaration(String name, List<String> parameters, List<Statement> body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }
}
