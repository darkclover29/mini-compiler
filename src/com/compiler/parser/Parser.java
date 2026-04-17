package com.compiler.parser;

import com.compiler.lexer.Token;
import com.compiler.lexer.Token.Type;
import com.compiler.ast.*;
import com.compiler.ast.statements.*;
import com.compiler.ast.expressions.*;

import java.util.*;

public class Parser {

    private List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Statement> parseProgram() {
        List<Statement> statements = new ArrayList<>();

        while (!match(Type.EOF)) {
            statements.add(parseStatement());
        }

        return statements;
    }

    private Statement parseStatement() {

        if (match(Type.PRINT)) {
            Expression expr = parseExpression();
            consume(Type.SEMICOLON);
            return new PrintStatement(expr);
        }

        if (match(Type.LET)) {
            String name = consume(Type.IDENTIFIER).value;
            consume(Type.EQ);
            Expression value = parseExpression();
            consume(Type.SEMICOLON);
            return new VarDeclaration(name, value);
        }

        if (match(Type.IF)) {
            consume(Type.LPAREN);
            Expression condition = parseExpression();
            consume(Type.RPAREN);

            List<Statement> thenBranch = parseBlock();
            List<Statement> elseBranch = new ArrayList<>();

            if (match(Type.ELSE)) {
                elseBranch = parseBlock();
            }

            return new IfStatement(condition, thenBranch, elseBranch);
        }

        if (match(Type.WHILE)) {
            consume(Type.LPAREN);
            Expression condition = parseExpression();
            consume(Type.RPAREN);

            List<Statement> body = parseBlock();
            return new WhileStatement(condition, body);
        }

        // Assignment
        if (check(Type.IDENTIFIER)) {
            String name = consume(Type.IDENTIFIER).value;
            consume(Type.EQ);
            Expression value = parseExpression();
            consume(Type.SEMICOLON);
            return new Assignment(name, value);
        }

        throw new ParseException("Invalid statement");
    }

    private List<Statement> parseBlock() {
        consume(Type.LBRACE);
        List<Statement> statements = new ArrayList<>();

        while (!check(Type.RBRACE)) {
            statements.add(parseStatement());
        }

        consume(Type.RBRACE);
        return statements;
    }

    private Expression parseExpression() {
        return parseEquality();
    }

    private Expression parseEquality() {
        Expression expr = parseComparison();

        while (match(Type.EQEQ)) {
            String op = "==";
            Expression right = parseComparison();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression parseComparison() {
        Expression expr = parseTerm();

        while (match(Type.LT) || match(Type.GT)) {
            String op = previous().value;
            Expression right = parseTerm();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression parseTerm() {
        Expression expr = parseFactor();

        while (match(Type.PLUS) || match(Type.MINUS)) {
            String op = previous().value;
            Expression right = parseFactor();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression parseFactor() {
        Expression expr = parsePrimary();

        while (match(Type.MUL) || match(Type.DIV)) {
            String op = previous().value;
            Expression right = parsePrimary();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression parsePrimary() {

        if (match(Type.NUMBER)) {
            return new NumberExpression(Integer.parseInt(previous().value));
        }

        if (match(Type.IDENTIFIER)) {
            return new VariableExpression(previous().value);
        }

        if (match(Type.LPAREN)) {
            Expression expr = parseExpression();
            consume(Type.RPAREN);
            return expr;
        }

        throw new ParseException("Invalid expression");
    }

    // Helpers
    private boolean match(Type type) {
        if (check(type)) {
            pos++;
            return true;
        }
        return false;
    }

    private boolean check(Type type) {
        return tokens.get(pos).type == type;
    }

    private Token consume(Type type) {
        if (check(type)) return tokens.get(pos++);
        throw new ParseException("Expected " + type);
    }

    private Token previous() {
        return tokens.get(pos - 1);
    }
}