package com.compiler.ast.statements;

import com.compiler.ast.Statement;
import java.util.List;

public class BlockStatement extends Statement {
    public final List<Statement> statements;

    public BlockStatement(List<Statement> statements) {
        this.statements = statements;
    }
}
