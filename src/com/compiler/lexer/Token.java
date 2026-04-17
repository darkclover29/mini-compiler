package com.compiler.lexer;

public class Token {

    public enum Type {
        PRINT, LET, IF, ELSE, WHILE,
        NUMBER, IDENTIFIER,
        PLUS, MINUS, MUL, DIV,
        LT, GT, EQ, EQEQ,
        LPAREN, RPAREN, LBRACE, RBRACE, SEMICOLON,
        EOF
    }

    public Type type;
    public String value;

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }
}