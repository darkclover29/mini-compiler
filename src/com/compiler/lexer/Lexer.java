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

            if (c == '"') {
                pos++;
                StringBuilder str = new StringBuilder();
                while (pos < input.length() && input.charAt(pos) != '"') {
                    char curr = input.charAt(pos);
                    if (curr == '\\' && pos + 1 < input.length()) {
                        pos++;
                        char next = input.charAt(pos);
                        if (next == 'n') str.append('\n');
                        else if (next == 't') str.append('\t');
                        else if (next == 'r') str.append('\r');
                        else if (next == '"') str.append('"');
                        else if (next == '\\') str.append('\\');
                        else str.append(next);
                    } else {
                        str.append(curr);
                    }
                    pos++;
                }
                if (pos >= input.length()) {
                    throw new RuntimeException("Unterminated string literal");
                }
                pos++;
                tokens.add(new Token(Token.Type.STRING, str.toString()));
                continue;
            }

            if (Character.isWhitespace(c)) {
                pos++;
                continue;
            }

            if (Character.isDigit(c)) {
                StringBuilder num = new StringBuilder();
                while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                    num.append(input.charAt(pos++));
                }
                if (pos < input.length() && input.charAt(pos) == '.' && pos + 1 < input.length() && Character.isDigit(input.charAt(pos + 1))) {
                    num.append(input.charAt(pos++)); // append '.'
                    while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                        num.append(input.charAt(pos++));
                    }
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
                    case "fun": tokens.add(new Token(Token.Type.FUN, w)); break;
                    case "return": tokens.add(new Token(Token.Type.RETURN, w)); break;
                    case "for": tokens.add(new Token(Token.Type.FOR, w)); break;
                    default: tokens.add(new Token(Token.Type.IDENTIFIER, w));
                }
                continue;
            }

            switch (c) {
                case '+': tokens.add(new Token(Token.Type.PLUS, "+")); break;
                case '-': tokens.add(new Token(Token.Type.MINUS, "-")); break;
                case '*': tokens.add(new Token(Token.Type.MUL, "*")); break;
                case '%': tokens.add(new Token(Token.Type.MOD, "%")); break;
                case '/':
                    if (pos + 1 < input.length() && input.charAt(pos + 1) == '/') {
                        while (pos < input.length() && input.charAt(pos) != '\n') {
                            pos++;
                        }
                        continue;
                    } else if (pos + 1 < input.length() && input.charAt(pos + 1) == '*') {
                        pos += 2;
                        while (pos < input.length() - 1 && !(input.charAt(pos) == '*' && input.charAt(pos + 1) == '/')) {
                            pos++;
                        }
                        pos += 2; // move past both '*' and '/' of the closing '*/'
                        continue;
                    } else {
                        tokens.add(new Token(Token.Type.DIV, "/"));
                    }
                    break;
                case '<':
                    if (pos + 1 < input.length() && input.charAt(pos + 1) == '=') {
                        tokens.add(new Token(Token.Type.LE, "<="));
                        pos++;
                    } else {
                        tokens.add(new Token(Token.Type.LT, "<"));
                    }
                    break;
                case '>':
                    if (pos + 1 < input.length() && input.charAt(pos + 1) == '=') {
                        tokens.add(new Token(Token.Type.GE, ">="));
                        pos++;
                    } else {
                        tokens.add(new Token(Token.Type.GT, ">"));
                    }
                    break;

                case '=':
                    if (pos + 1 < input.length() && input.charAt(pos + 1) == '=') {
                        tokens.add(new Token(Token.Type.EQEQ, "=="));
                        pos++;
                    } else {
                        tokens.add(new Token(Token.Type.EQ, "="));
                    }
                    break;

                case '!':
                    if (pos + 1 < input.length() && input.charAt(pos + 1) == '=') {
                        tokens.add(new Token(Token.Type.BANG_EQ, "!="));
                        pos++;
                    } else {
                        tokens.add(new Token(Token.Type.BANG, "!"));
                    }
                    break;

                case '&':
                    if (pos + 1 < input.length() && input.charAt(pos + 1) == '&') {
                        tokens.add(new Token(Token.Type.AND, "&&"));
                        pos++;
                    } else {
                        throw new RuntimeException("Unexpected char: &");
                    }
                    break;

                case '|':
                    if (pos + 1 < input.length() && input.charAt(pos + 1) == '|') {
                        tokens.add(new Token(Token.Type.OR, "||"));
                        pos++;
                    } else {
                        throw new RuntimeException("Unexpected char: |");
                    }
                    break;

                case ',': tokens.add(new Token(Token.Type.COMMA, ",")); break;
                case '[': tokens.add(new Token(Token.Type.LBRACKET, "[")); break;
                case ']': tokens.add(new Token(Token.Type.RBRACKET, "]")); break;
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