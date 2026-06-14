package com.compiler.lexer;

public class Token {

    public enum Type {
        PRINT, LET, IF, ELSE, WHILE, FUN, RETURN, FOR,
        NUMBER, IDENTIFIER, STRING,
        PLUS, MINUS, MUL, DIV, MOD,
        LT, GT, LE, GE, EQ, EQEQ, BANG_EQ,
        AND, OR, BANG, COMMA,
        LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET, SEMICOLON,
        EOF
    }

    public Type type;
    public String value;

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }
}