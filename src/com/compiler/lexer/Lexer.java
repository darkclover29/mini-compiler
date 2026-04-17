package com.compiler.lexer;

import java.util.*;

public class Lexer {

    private String input;
    private int pos = 0;

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (pos < input.length()) {
            char c = input.charAt(pos);

            if (Character.isWhitespace(c)) {
                pos++;
                continue;
            }

            if (Character.isDigit(c)) {
                StringBuilder num = new StringBuilder();
                while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                    num.append(input.charAt(pos++));
                }
                tokens.add(new Token(Token.Type.NUMBER, num.toString()));
                continue;
            }

            if (Character.isLetter(c)) {
                StringBuilder word = new StringBuilder();
                while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos))) {
                    word.append(input.charAt(pos++));
                }

                String w = word.toString();

                switch (w) {
                    case "print": tokens.add(new Token(Token.Type.PRINT, w)); break;
                    case "let": tokens.add(new Token(Token.Type.LET, w)); break;
                    case "if": tokens.add(new Token(Token.Type.IF, w)); break;
                    case "else": tokens.add(new Token(Token.Type.ELSE, w)); break;
                    case "while": tokens.add(new Token(Token.Type.WHILE, w)); break;
                    default: tokens.add(new Token(Token.Type.IDENTIFIER, w));
                }
                continue;
            }

            switch (c) {
                case '+': tokens.add(new Token(Token.Type.PLUS, "+")); break;
                case '-': tokens.add(new Token(Token.Type.MINUS, "-")); break;
                case '*': tokens.add(new Token(Token.Type.MUL, "*")); break;
                case '/': tokens.add(new Token(Token.Type.DIV, "/")); break;
                case '<': tokens.add(new Token(Token.Type.LT, "<")); break;
                case '>': tokens.add(new Token(Token.Type.GT, ">")); break;

                case '=':
                    if (pos + 1 < input.length() && input.charAt(pos + 1) == '=') {
                        tokens.add(new Token(Token.Type.EQEQ, "=="));
                        pos++;
                    } else {
                        tokens.add(new Token(Token.Type.EQ, "="));
                    }
                    break;

                case '(': tokens.add(new Token(Token.Type.LPAREN, "(")); break;
                case ')': tokens.add(new Token(Token.Type.RPAREN, ")")); break;
                case '{': tokens.add(new Token(Token.Type.LBRACE, "{")); break;
                case '}': tokens.add(new Token(Token.Type.RBRACE, "}")); break;
                case ';': tokens.add(new Token(Token.Type.SEMICOLON, ";")); break;

                default: throw new RuntimeException("Unexpected char: " + c);
            }

            pos++;
        }

        tokens.add(new Token(Token.Type.EOF, ""));
        return tokens;
    }
}