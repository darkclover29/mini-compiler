package com.compiler.utils;

import com.compiler.lexer.*;
import com.compiler.parser.*;
import com.compiler.interpreter.*;
import com.compiler.ast.Statement;

import java.nio.file.*;
import java.util.*;

public class Runner {

    public static void run(String path) throws Exception {

        String code = new String(Files.readAllBytes(Paths.get(path)));

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        Parser parser = new Parser(tokens);
        List<Statement> program = parser.parseProgram();

        Interpreter interpreter = new Interpreter();
        interpreter.execute(program);
    }
}