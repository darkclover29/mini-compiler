package com.compiler.ast.statements;

import com.compiler.ast.Statement;
import com.compiler.ast.expressions.FunctionCall;

public class CallStatement extends Statement {
    public final FunctionCall call;

    public CallStatement(FunctionCall call) {
        this.call = call;
    }
}
