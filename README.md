# Mini Compiler (Custom Programming Language in Java)

## Overview

This project is a custom programming language and interpreter built entirely in Java.

It allows users to write programs in a simple, self-defined language and execute them through a compiler-like pipeline. The goal of this project is to understand how programming languages work internally, including how code is tokenized, parsed, and executed.

---

## Motivation

This project was built to go beyond standard application development and explore the internal workings of programming languages.

It demonstrates core concepts such as:

* How source code is processed
* How syntax is validated
* How execution is performed without relying on an existing language runtime

---

## Features

### Language Capabilities

* Variable Declaration

  ```
  let x = 10;
  ```

* Arithmetic Operations

  ```
  print 5 + 3 * 2;
  ```

* Conditional Statements

  ```
  if (x > 5) {
      print 100;
  } else {
      print 0;
  }
  ```

* While Loops

  ```
  while (x > 0) {
      print x;
      x = x - 1;
  }
  ```

* Comparison Operators

    * `<`
    * `>`
    * `==`

---

## Project Structure

```
mini-compiler/
│
├── lexer/        Converts source code into tokens
├── parser/       Builds an Abstract Syntax Tree (AST)
├── ast/          Defines program structure (nodes)
├── interpreter/  Executes the program
├── utils/        Handles file execution
└── program.txt   Input program written in custom language
```

---

## How It Works

### 1. Lexical Analysis (Lexer)

The lexer reads raw source code and converts it into tokens.

Example:

```
print 5;
```

Becomes:

```
PRINT, NUMBER(5), SEMICOLON
```

---

### 2. Parsing (Syntax Analysis)

The parser converts tokens into an Abstract Syntax Tree (AST), which represents the structure of the program.

Example:

```
print 5 + 3;
```

Is transformed into a tree representing the operation:

* print

    * binary expression (+)

        * 5
        * 3

---

### 3. Interpretation (Execution)

The interpreter walks through the AST and executes each statement step by step.

---

## How to Run

### 1. Write your program

Edit `program.txt` with your custom language:

```
let x = 3;

while (x > 0) {
    print x;
    x = x - 1;
}
```

---

### 2. Compile and run

```
javac -d out src/com/compiler/**/*.java
java -cp out com.compiler.Main
```

---

## Example Output

```
3
2
1
```

---

## Concepts Covered

* Compiler Design Fundamentals
* Lexical Analysis
* Recursive Descent Parsing
* Abstract Syntax Trees (AST)
* Expression Evaluation
* Control Flow Execution
* Variable Management

---

## Future Improvements

* Add support for functions
* Add strings and arrays
* Build an interactive REPL
* Compile to bytecode or another target language

---

## Author

Harsh Tiwari

---

## Summary

This project demonstrates how a programming language can be built from scratch, including parsing and execution. It provides a practical understanding of compiler design and runtime behavior, which is useful for backend development, systems programming, and technical interviews.
