package com.compiler.utils;

import com.compiler.ast.Statement;
import com.compiler.interpreter.Interpreter;
import com.compiler.lexer.Lexer;
import com.compiler.lexer.Token;
import com.compiler.parser.Parser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class WebServer {

    private final int port;

    public WebServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Static assets
        server.createContext("/", new StaticFileHandler("web/index.html", "text/html"));
        server.createContext("/index.css", new StaticFileHandler("web/index.css", "text/css"));
        server.createContext("/index.js", new StaticFileHandler("web/index.js", "application/javascript"));

        // API Endpoint
        server.createContext("/api/run", new RunHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Mini Compiler Server started at http://localhost:" + port);
    }

    static class StaticFileHandler implements HttpHandler {
        private final String filePath;
        private final String contentType;

        StaticFileHandler(String filePath, String contentType) {
            this.filePath = filePath;
            this.contentType = contentType;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            byte[] bytes = Files.readAllBytes(path);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class RunHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            InputStream is = exchange.getRequestBody();
            String code = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            String responseJson;
            try {
                // 1. Lexing
                Lexer lexer = new Lexer(code);
                List<Token> tokens = lexer.tokenize();
                String tokensJson = TokenSerializer.toJson(tokens);

                // 2. Parsing
                Parser parser = new Parser(tokens);
                List<Statement> statements = parser.parseProgram();
                String astJson = AstSerializer.toJson(statements);

                // 3. Execution (Capturing Output)
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8);
                PrintStream oldOut = System.out;

                try {
                    System.setOut(ps);
                    Interpreter interpreter = new Interpreter();
                    interpreter.execute(statements);
                } finally {
                    System.setOut(oldOut);
                    ps.close();
                }

                String output = baos.toString(StandardCharsets.UTF_8);
                responseJson = String.format("{\"success\":true,\"output\":\"%s\",\"tokens\":%s,\"ast\":%s}", 
                    escape(output), tokensJson, astJson);

            } catch (Exception e) {
                // Capture compilation or execution error
                responseJson = String.format("{\"success\":false,\"error\":\"%s\"}", 
                    escape(e.getMessage() != null ? e.getMessage() : e.toString()));
            }

            byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String escape(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
    }
}
