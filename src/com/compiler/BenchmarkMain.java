package com.compiler;

import com.compiler.ast.Statement;
import com.compiler.interpreter.Interpreter;
import com.compiler.lexer.Lexer;
import com.compiler.lexer.Token;
import com.compiler.parser.Parser;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BenchmarkMain {

    private static final int WARMUP_RUNS = 20;
    private static final int MEASURED_RUNS = 50;

    public static void main(String[] args) {
        System.out.println("Mini Compiler Performance Benchmark");
        System.out.println("Warmup runs: " + WARMUP_RUNS + ", measured runs: " + MEASURED_RUNS);
        System.out.println();

        List<BenchmarkCase> cases = new ArrayList<>();
        cases.add(new BenchmarkCase("small-mixed", buildMixedProgram(50), false));
        cases.add(new BenchmarkCase("medium-mixed", buildMixedProgram(500), false));
        cases.add(new BenchmarkCase("large-mixed", buildMixedProgram(2000), false));
        cases.add(new BenchmarkCase("exec-heavy", buildExecutionHeavyProgram(200_000), false));

        try {
            String fileProgram = Files.readString(Path.of("program.txt"));
            cases.add(new BenchmarkCase("program-txt", fileProgram, true));
        } catch (Exception e) {
            System.out.println("Skipping program-txt case: " + e.getMessage());
        }

        for (BenchmarkCase benchCase : cases) {
            runCase(benchCase);
            System.out.println();
        }
    }

    private static void runCase(BenchmarkCase benchCase) {
        String code = benchCase.code;

        for (int i = 0; i < WARMUP_RUNS; i++) {
            executeOnce(code, benchCase.muteOutput);
        }

        List<Long> lexTimes = new ArrayList<>();
        List<Long> parseTimes = new ArrayList<>();
        List<Long> execTimes = new ArrayList<>();
        List<Long> totalTimes = new ArrayList<>();

        int tokenCount = 0;
        int statementCount = 0;

        for (int i = 0; i < MEASURED_RUNS; i++) {
            RunResult result = executeOnce(code, benchCase.muteOutput);
            lexTimes.add(result.lexNanos);
            parseTimes.add(result.parseNanos);
            execTimes.add(result.execNanos);
            totalTimes.add(result.totalNanos);
            tokenCount = result.tokenCount;
            statementCount = result.statementCount;
        }

        System.out.println("Case: " + benchCase.name);
        System.out.println("Source length: " + code.length() + " chars");
        System.out.println("Tokens: " + tokenCount + ", AST statements: " + statementCount);
        printMetric("lex", lexTimes);
        printMetric("parse", parseTimes);
        printMetric("execute", execTimes);
        printMetric("total", totalTimes);
    }

    private static RunResult executeOnce(String code, boolean muteOutput) {
        long totalStart = System.nanoTime();

        long lexStart = System.nanoTime();
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        long lexEnd = System.nanoTime();

        long parseStart = System.nanoTime();
        Parser parser = new Parser(tokens);
        List<Statement> program = parser.parseProgram();
        long parseEnd = System.nanoTime();

        long execStart = System.nanoTime();
        if (muteOutput) {
            withMutedStdOut(() -> {
                Interpreter interpreter = new Interpreter();
                interpreter.execute(program);
            });
        } else {
            Interpreter interpreter = new Interpreter();
            interpreter.execute(program);
        }
        long execEnd = System.nanoTime();

        long totalEnd = System.nanoTime();

        return new RunResult(
                lexEnd - lexStart,
                parseEnd - parseStart,
                execEnd - execStart,
                totalEnd - totalStart,
                tokens.size(),
                program.size()
        );
    }

    private static void printMetric(String label, List<Long> nanos) {
        double avgMs = average(nanos) / 1_000_000.0;
        double minMs = Collections.min(nanos) / 1_000_000.0;
        double maxMs = Collections.max(nanos) / 1_000_000.0;
        double p95Ms = percentile(nanos, 95) / 1_000_000.0;

        System.out.printf("%-8s avg=%8.3f ms  min=%8.3f ms  p95=%8.3f ms  max=%8.3f ms%n",
                label, avgMs, minMs, p95Ms, maxMs);
    }

    private static double average(List<Long> values) {
        long sum = 0;
        for (long value : values) {
            sum += value;
        }
        return (double) sum / values.size();
    }

    private static long percentile(List<Long> values, int p) {
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil((p / 100.0) * sorted.size()) - 1;
        if (index < 0) {
            index = 0;
        }
        if (index >= sorted.size()) {
            index = sorted.size() - 1;
        }
        return sorted.get(index);
    }

    private static String buildMixedProgram(int statements) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < statements; i++) {
            sb.append("let x").append(i).append(" = ").append(i).append(" + ").append(i + 1).append(" * 2;\n");
            if (i % 7 == 0) {
                sb.append("if (x").append(i).append(" > 10) { let y").append(i).append(" = x").append(i).append(" - 1; } else { let y")
                        .append(i).append(" = 0; }\n");
            }
        }
        return sb.toString();
    }

    private static String buildExecutionHeavyProgram(int loopCount) {
        return "let x = " + loopCount + ";\n"
                + "while (x > 0) {\n"
                + "    x = x - 1;\n"
                + "}\n";
    }

    private static void withMutedStdOut(Runnable action) {
        PrintStream originalOut = System.out;
        PrintStream sink = new PrintStream(OutputStream.nullOutputStream());
        try {
            System.setOut(sink);
            action.run();
        } finally {
            System.setOut(originalOut);
            sink.close();
        }
    }

    private static class BenchmarkCase {
        final String name;
        final String code;
        final boolean muteOutput;

        BenchmarkCase(String name, String code, boolean muteOutput) {
            this.name = name;
            this.code = code;
            this.muteOutput = muteOutput;
        }
    }

    private static class RunResult {
        final long lexNanos;
        final long parseNanos;
        final long execNanos;
        final long totalNanos;
        final int tokenCount;
        final int statementCount;

        RunResult(long lexNanos, long parseNanos, long execNanos, long totalNanos, int tokenCount, int statementCount) {
            this.lexNanos = lexNanos;
            this.parseNanos = parseNanos;
            this.execNanos = execNanos;
            this.totalNanos = totalNanos;
            this.tokenCount = tokenCount;
            this.statementCount = statementCount;
        }
    }
}
