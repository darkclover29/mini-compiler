package com.compiler.interpreter;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    private final Environment parent;
    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, Function> functions = new HashMap<>();

    public Environment() {
        this.parent = null;
    }

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public Environment getParent() {
        return parent;
    }

    public void define(String name, Object value) {
        variables.put(name, value);
    }

    public void assign(String name, Object value) {
        if (variables.containsKey(name)) {
            variables.put(name, value);
        } else if (parent != null) {
            parent.assign(name, value);
        } else {
            throw new RuntimeException("Undefined variable: " + name);
        }
    }

    public Object get(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else if (parent != null) {
            return parent.get(name);
        }
        throw new RuntimeException("Variable not defined: " + name);
    }

    public void defineFunction(String name, Function function) {
        functions.put(name, function);
    }

    public Function getFunction(String name) {
        if (functions.containsKey(name)) {
            return functions.get(name);
        } else if (parent != null) {
            return parent.getFunction(name);
        }
        throw new RuntimeException("Function not defined: " + name);
    }
}