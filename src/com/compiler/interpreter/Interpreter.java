package com.compiler.interpreter;

import com.compiler.ast.*;
import com.compiler.ast.statements.*;
import com.compiler.ast.expressions.*;

import java.util.List;
import java.util.ArrayList;

public class Interpreter {

    private Environment env = new Environment();

    public void execute(List<Statement> statements) {
        for (Statement stmt : statements) {
            executeStatement(stmt);
        }
    }

    private void executeBlock(List<Statement> statements, Environment blockEnv) {
        Environment previous = this.env;
        try {
            this.env = blockEnv;
            for (Statement stmt : statements) {
                executeStatement(stmt);
            }
        } finally {
            this.env = previous;
        }
    }

    private void executeStatement(Statement stmt) {

        if (stmt instanceof PrintStatement) {
            Object value = evaluate(((PrintStatement) stmt).expression);
            System.out.println(formatValue(value));
        }

        else if (stmt instanceof VarDeclaration) {
            VarDeclaration vd = (VarDeclaration) stmt;
            env.define(vd.name, evaluate(vd.value));
        }

        else if (stmt instanceof Assignment) {
            Assignment as = (Assignment) stmt;
            env.assign(as.name, evaluate(as.value));
        }

        else if (stmt instanceof IfStatement) {
            IfStatement ifs = (IfStatement) stmt;
            if (isTruthy(evaluate(ifs.condition))) {
                executeBlock(ifs.thenBranch, new Environment(env));
            } else {
                executeBlock(ifs.elseBranch, new Environment(env));
            }
        }

        else if (stmt instanceof WhileStatement) {
            WhileStatement ws = (WhileStatement) stmt;
            while (isTruthy(evaluate(ws.condition))) {
                executeBlock(ws.body, new Environment(env));
            }
        }

        else if (stmt instanceof FunctionDeclaration) {
            FunctionDeclaration fd = (FunctionDeclaration) stmt;
            env.defineFunction(fd.name, new Function(fd, env));
        }

        else if (stmt instanceof ReturnStatement) {
            ReturnStatement rs = (ReturnStatement) stmt;
            throw new ReturnException(evaluate(rs.expression));
        }

        else if (stmt instanceof CallStatement) {
            CallStatement cs = (CallStatement) stmt;
            evaluate(cs.call);
        }

        else if (stmt instanceof BlockStatement) {
            BlockStatement bs = (BlockStatement) stmt;
            executeBlock(bs.statements, new Environment(env));
        }

        else if (stmt instanceof IndexAssignment) {
            IndexAssignment ia = (IndexAssignment) stmt;
            Object targetVal = evaluate(ia.target.target);
            Object indexVal = evaluate(ia.target.index);
            Object valueVal = evaluate(ia.value);

            if (targetVal instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) targetVal;
                int idx = ((Number) indexVal).intValue();
                list.set(idx, valueVal);
            } else {
                throw new RuntimeException("Can only assign index elements in arrays");
            }
        }
    }

    private Object evaluate(Expression expr) {

        if (expr instanceof NumberExpression) {
            return ((NumberExpression) expr).value;
        }

        if (expr instanceof StringExpression) {
            return ((StringExpression) expr).value;
        }

        if (expr instanceof VariableExpression) {
            return env.get(((VariableExpression) expr).name);
        }

        if (expr instanceof ArrayExpression) {
            ArrayExpression ae = (ArrayExpression) expr;
            List<Object> elements = new ArrayList<>();
            for (Expression elem : ae.elements) {
                elements.add(evaluate(elem));
            }
            return elements;
        }

        if (expr instanceof IndexExpression) {
            IndexExpression ie = (IndexExpression) expr;
            Object targetVal = evaluate(ie.target);
            Object indexVal = evaluate(ie.index);

            if (targetVal instanceof List) {
                List<?> list = (List<?>) targetVal;
                int idx = ((Number) indexVal).intValue();
                return list.get(idx);
            } else if (targetVal instanceof String) {
                String str = (String) targetVal;
                int idx = ((Number) indexVal).intValue();
                return String.valueOf(str.charAt(idx));
            } else {
                throw new RuntimeException("Can only index arrays or strings");
            }
        }

        if (expr instanceof UnaryExpression) {
            UnaryExpression ue = (UnaryExpression) expr;
            Object right = evaluate(ue.expression);
            switch (ue.operator) {
                case "!": return !isTruthy(right) ? 1.0 : 0.0;
                case "-":
                    if (right instanceof Double) return -(Double) right;
                    return -((Number) right).doubleValue();
            }
            throw new RuntimeException("Unknown unary operator: " + ue.operator);
        }

        if (expr instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) expr;

            // Check for built-ins
            if (fc.name.equals("len")) {
                if (fc.arguments.size() != 1) {
                    throw new RuntimeException("len() expects exactly 1 argument");
                }
                Object arg = evaluate(fc.arguments.get(0));
                if (arg instanceof List) {
                    return (double) ((List<?>) arg).size();
                } else if (arg instanceof String) {
                    return (double) ((String) arg).length();
                } else {
                    throw new RuntimeException("len() argument must be an array or a string");
                }
            }

            if (fc.name.equals("append")) {
                if (fc.arguments.size() != 2) {
                    throw new RuntimeException("append() expects exactly 2 arguments");
                }
                Object arg1 = evaluate(fc.arguments.get(0));
                Object arg2 = evaluate(fc.arguments.get(1));
                if (arg1 instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> list = (List<Object>) arg1;
                    list.add(arg2);
                    return list;
                } else {
                    throw new RuntimeException("append() first argument must be an array");
                }
            }

            if (fc.name.equals("type")) {
                if (fc.arguments.size() != 1) {
                    throw new RuntimeException("type() expects exactly 1 argument");
                }
                Object arg = evaluate(fc.arguments.get(0));
                if (arg instanceof Double || arg instanceof Integer) {
                    return "number";
                } else if (arg instanceof String) {
                    return "string";
                } else if (arg instanceof List) {
                    return "array";
                } else if (arg instanceof Function) {
                    return "function";
                } else {
                    return "unknown";
                }
            }

            Function func = env.getFunction(fc.name);

            if (func.declaration.parameters.size() != fc.arguments.size()) {
                throw new RuntimeException("Argument count mismatch for function: " + fc.name);
            }

            Environment functionEnv = new Environment(func.closure);
            for (int i = 0; i < func.declaration.parameters.size(); i++) {
                functionEnv.define(func.declaration.parameters.get(i), evaluate(fc.arguments.get(i)));
            }

            try {
                executeBlock(func.declaration.body, functionEnv);
            } catch (ReturnException re) {
                return re.value;
            }
            return 0.0;
        }

        if (expr instanceof BinaryExpression) {
            BinaryExpression be = (BinaryExpression) expr;

            if (be.operator.equals("&&")) {
                Object left = evaluate(be.left);
                if (!isTruthy(left)) return 0.0;
                return isTruthy(evaluate(be.right)) ? 1.0 : 0.0;
            }
            if (be.operator.equals("||")) {
                Object left = evaluate(be.left);
                if (isTruthy(left)) return 1.0;
                return isTruthy(evaluate(be.right)) ? 1.0 : 0.0;
            }

            Object leftVal = evaluate(be.left);
            Object rightVal = evaluate(be.right);

            switch (be.operator) {
                case "+":
                    if (leftVal instanceof String || rightVal instanceof String) {
                        return formatValue(leftVal) + formatValue(rightVal);
                    }
                    return ((Number) leftVal).doubleValue() + ((Number) rightVal).doubleValue();
                case "-": return ((Number) leftVal).doubleValue() - ((Number) rightVal).doubleValue();
                case "*": return ((Number) leftVal).doubleValue() * ((Number) rightVal).doubleValue();
                case "/": return ((Number) leftVal).doubleValue() / ((Number) rightVal).doubleValue();
                case "%": return ((Number) leftVal).doubleValue() % ((Number) rightVal).doubleValue();
                case "<":
                    if (leftVal instanceof String && rightVal instanceof String) {
                        return ((String) leftVal).compareTo((String) rightVal) < 0 ? 1.0 : 0.0;
                    }
                    return ((Number) leftVal).doubleValue() < ((Number) rightVal).doubleValue() ? 1.0 : 0.0;
                case ">":
                    if (leftVal instanceof String && rightVal instanceof String) {
                        return ((String) leftVal).compareTo((String) rightVal) > 0 ? 1.0 : 0.0;
                    }
                    return ((Number) leftVal).doubleValue() > ((Number) rightVal).doubleValue() ? 1.0 : 0.0;
                case "<=":
                    if (leftVal instanceof String && rightVal instanceof String) {
                        return ((String) leftVal).compareTo((String) rightVal) <= 0 ? 1.0 : 0.0;
                    }
                    return ((Number) leftVal).doubleValue() <= ((Number) rightVal).doubleValue() ? 1.0 : 0.0;
                case ">=":
                    if (leftVal instanceof String && rightVal instanceof String) {
                        return ((String) leftVal).compareTo((String) rightVal) >= 0 ? 1.0 : 0.0;
                    }
                    return ((Number) leftVal).doubleValue() >= ((Number) rightVal).doubleValue() ? 1.0 : 0.0;
                case "==":
                    if (leftVal == null) return rightVal == null ? 1.0 : 0.0;
                    return leftVal.equals(rightVal) ? 1.0 : 0.0;
                case "!=":
                    if (leftVal == null) return rightVal != null ? 1.0 : 0.0;
                    return !leftVal.equals(rightVal) ? 1.0 : 0.0;
            }
        }

        throw new RuntimeException("Invalid Expression");
    }

    private boolean isTruthy(Object val) {
        if (val == null) return false;
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Integer) return (Integer) val != 0;
        if (val instanceof Double) return (Double) val != 0.0;
        if (val instanceof String) return !((String) val).isEmpty();
        return true;
    }

    private String formatValue(Object value) {
        if (value instanceof Double) {
            double d = (Double) value;
            if (d == (long) d) {
                return String.valueOf((long) d);
            } else {
                return String.valueOf(d);
            }
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < list.size(); i++) {
                sb.append(formatValue(list.get(i)));
                if (i < list.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
            return sb.toString();
        }
        return String.valueOf(value);
    }
}