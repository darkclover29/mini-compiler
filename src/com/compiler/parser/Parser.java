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

        if (match(Type.FOR)) {
            consume(Type.LPAREN);
            Statement initializer = null;
            if (!match(Type.SEMICOLON)) {
                if (match(Type.LET)) {
                    String name = consume(Type.IDENTIFIER).value;
                    consume(Type.EQ);
                    Expression value = parseExpression();
                    consume(Type.SEMICOLON);
                    initializer = new VarDeclaration(name, value);
                } else {
                    String name = consume(Type.IDENTIFIER).value;
                    consume(Type.EQ);
                    Expression value = parseExpression();
                    consume(Type.SEMICOLON);
                    initializer = new Assignment(name, value);
                }
            }

            Expression condition = null;
            if (!check(Type.SEMICOLON)) {
                condition = parseExpression();
            }
            consume(Type.SEMICOLON);

            Statement increment = null;
            if (!check(Type.RPAREN)) {
                if (check(Type.IDENTIFIER)) {
                    String name = consume(Type.IDENTIFIER).value;
                    if (match(Type.EQ)) {
                        Expression value = parseExpression();
                        increment = new Assignment(name, value);
                    }
                }
            }
            consume(Type.RPAREN);

            List<Statement> body = parseBlock();
            if (increment != null) {
                body.add(increment);
            }
            if (condition == null) {
                condition = new NumberExpression(1);
            }

            Statement whileLoop = new WhileStatement(condition, body);

            if (initializer != null) {
                List<Statement> blockStmts = new ArrayList<>();
                blockStmts.add(initializer);
                blockStmts.add(whileLoop);
                return new BlockStatement(blockStmts);
            }
            return whileLoop;
        }

        if (match(Type.FUN)) {
            return parseFunctionDeclaration();
        }

        if (match(Type.RETURN)) {
            Expression expr = parseExpression();
            consume(Type.SEMICOLON);
            return new ReturnStatement(expr);
        }

        if (check(Type.LBRACE)) {
            return new BlockStatement(parseBlock());
        }

        // Assignment, Call, or Index Assignment Statement
        if (check(Type.IDENTIFIER)) {
            String name = consume(Type.IDENTIFIER).value;
            if (match(Type.EQ)) {
                Expression value = parseExpression();
                consume(Type.SEMICOLON);
                return new Assignment(name, value);
            } else if (match(Type.LPAREN)) {
                List<Expression> arguments = new ArrayList<>();
                if (!check(Type.RPAREN)) {
                    do {
                        arguments.add(parseExpression());
                    } while (match(Type.COMMA));
                }
                consume(Type.RPAREN);
                consume(Type.SEMICOLON);
                return new CallStatement(new FunctionCall(name, arguments));
            } else if (check(Type.LBRACKET)) {
                Expression target = new VariableExpression(name);
                while (match(Type.LBRACKET)) {
                    Expression index = parseExpression();
                    consume(Type.RBRACKET);
                    target = new IndexExpression(target, index);
                }
                consume(Type.EQ);
                Expression value = parseExpression();
                consume(Type.SEMICOLON);
                if (target instanceof IndexExpression) {
                    return new IndexAssignment((IndexExpression) target, value);
                } else {
                    throw new ParseException("Invalid assignment target");
                }
            } else {
                throw new ParseException("Expected '=', '(', or '[' after identifier: " + name);
            }
        }

        throw new ParseException("Invalid statement: " + tokens.get(pos).value);
    }

    private Statement parseFunctionDeclaration() {
        String name = consume(Type.IDENTIFIER).value;
        consume(Type.LPAREN);
        List<String> parameters = new ArrayList<>();
        if (!check(Type.RPAREN)) {
            do {
                parameters.add(consume(Type.IDENTIFIER).value);
            } while (match(Type.COMMA));
        }
        consume(Type.RPAREN);
        List<Statement> body = parseBlock();
        return new FunctionDeclaration(name, parameters, body);
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
        return parseLogicalOr();
    }

    private Expression parseLogicalOr() {
        Expression expr = parseLogicalAnd();

        while (match(Type.OR)) {
            String op = "||";
            Expression right = parseLogicalAnd();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression parseLogicalAnd() {
        Expression expr = parseEquality();

        while (match(Type.AND)) {
            String op = "&&";
            Expression right = parseEquality();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression parseEquality() {
        Expression expr = parseComparison();

        while (match(Type.EQEQ) || match(Type.BANG_EQ)) {
            String op = previous().value;
            Expression right = parseComparison();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression parseComparison() {
        Expression expr = parseTerm();

        while (match(Type.LT) || match(Type.GT) || match(Type.LE) || match(Type.GE)) {
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
        Expression expr = parseUnary();

        while (match(Type.MUL) || match(Type.DIV) || match(Type.MOD)) {
            String op = previous().value;
            Expression right = parseUnary();
            expr = new BinaryExpression(expr, op, right);
        }

        return expr;
    }

    private Expression parseUnary() {
        if (match(Type.BANG) || match(Type.MINUS)) {
            String op = previous().value;
            Expression right = parseUnary();
            return new UnaryExpression(op, right);
        }

        return parseCall();
    }

    private Expression parseCall() {
        Expression expr = parsePrimary();

        while (true) {
            if (match(Type.LPAREN)) {
                if (expr instanceof VariableExpression) {
                    String name = ((VariableExpression) expr).name;
                    List<Expression> arguments = new ArrayList<>();
                    if (!check(Type.RPAREN)) {
                        do {
                            arguments.add(parseExpression());
                        } while (match(Type.COMMA));
                    }
                    consume(Type.RPAREN);
                    expr = new FunctionCall(name, arguments);
                } else {
                    throw new ParseException("Can only call variables/functions");
                }
            } else if (match(Type.LBRACKET)) {
                Expression index = parseExpression();
                consume(Type.RBRACKET);
                expr = new IndexExpression(expr, index);
            } else {
                break;
            }
        }

        return expr;
    }

    private Expression parsePrimary() {

        if (match(Type.NUMBER)) {
            return new NumberExpression(Double.parseDouble(previous().value));
        }

        if (match(Type.STRING)) {
            return new StringExpression(previous().value);
        }

        if (match(Type.IDENTIFIER)) {
            return new VariableExpression(previous().value);
        }

        if (match(Type.LBRACKET)) {
            List<Expression> elements = new ArrayList<>();
            if (!check(Type.RBRACKET)) {
                do {
                    elements.add(parseExpression());
                } while (match(Type.COMMA));
            }
            consume(Type.RBRACKET);
            return new ArrayExpression(elements);
        }

        if (match(Type.LPAREN)) {
            Expression expr = parseExpression();
            consume(Type.RPAREN);
            return expr;
        }

        throw new ParseException("Invalid expression: " + tokens.get(pos).value);
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