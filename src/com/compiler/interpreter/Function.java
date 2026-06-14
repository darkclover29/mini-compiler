package com.compiler.interpreter;

import com.compiler.ast.statements.FunctionDeclaration;

public class Function {
    public final FunctionDeclaration declaration;
    public final Environment closure;

    public Function(FunctionDeclaration declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }
}
