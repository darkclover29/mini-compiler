// Canvas Glowing Node Network Simulation
const canvas = document.getElementById('canvas-network');
const ctx = canvas.getContext('2d');

let particles = [];
const particleCount = 45;
const connectionDist = 120;

function resizeCanvas() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
}
window.addEventListener('resize', resizeCanvas);
resizeCanvas();

class Particle {
    constructor() {
        this.reset();
    }
    reset() {
        this.x = Math.random() * canvas.width;
        this.y = Math.random() * canvas.height;
        this.vx = (Math.random() - 0.5) * 0.4;
        this.vy = (Math.random() - 0.5) * 0.4;
        this.radius = Math.random() * 2 + 1;
    }
    update() {
        this.x += this.vx;
        this.y += this.vy;
        if (this.x < 0 || this.x > canvas.width || this.y < 0 || this.y > canvas.height) {
            this.reset();
        }
    }
    draw() {
        ctx.beginPath();
        ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2);
        ctx.fillStyle = getComputedStyle(document.documentElement).getPropertyValue('--accent-cyan').trim() || '#06b6d4';
        ctx.fill();
    }
}

for (let i = 0; i < particleCount; i++) {
    particles.push(new Particle());
}

function hexToRgb(hex) {
    hex = hex.replace('#', '').trim();
    if (hex.length === 3) {
        hex = hex.split('').map(x => x + x).join('');
    }
    const num = parseInt(hex, 16);
    return `${(num >> 16) & 255}, ${(num >> 8) & 255}, ${num & 255}`;
}

function drawNetwork() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const accentCyan = getComputedStyle(document.documentElement).getPropertyValue('--accent-cyan').trim() || '#06b6d4';
    
    for (let i = 0; i < particles.length; i++) {
        const p1 = particles[i];
        p1.update();
        p1.draw();
        
        for (let j = i + 1; j < particles.length; j++) {
            const p2 = particles[j];
            const dx = p1.x - p2.x;
            const dy = p1.y - p2.y;
            const dist = Math.sqrt(dx * dx + dy * dy);
            
            if (dist < connectionDist) {
                ctx.beginPath();
                ctx.moveTo(p1.x, p1.y);
                ctx.lineTo(p2.x, p2.y);
                ctx.strokeStyle = `rgba(${hexToRgb(accentCyan)}, ${0.08 * (1 - dist / connectionDist)})`;
                ctx.lineWidth = 0.8;
                ctx.stroke();
            }
        }
    }
    requestAnimationFrame(drawNetwork);
}
drawNetwork();

// Editor elements
const editor = document.getElementById('editor');
const lineNumbers = document.getElementById('line-numbers');
const runBtn = document.getElementById('run-btn');
const downloadBtn = document.getElementById('download-btn');
const copyBtn = document.getElementById('copy-btn');
const examplesSelect = document.getElementById('examples-select');
const themeSelect = document.getElementById('theme-select');
const terminalOutput = document.getElementById('terminal-output');
const tokensList = document.getElementById('tokens-list');
const astContainer = document.getElementById('ast-container');
const tabButtons = document.querySelectorAll('.tab-btn');
const tabContents = document.querySelectorAll('.tab-content');

// Code Preset Examples
const presets = {
    factorial: `// Factorial function demonstrating recursion, if-else, returns
fun fact(n) {
    if (n <= 1) {
        return 1;
    }
    return n * fact(n - 1);
}

let result = fact(5);
print "Factorial of 5 is: " + result;`,

    fibonacci: `// Fibonacci sequence calculator using loops
let count = 8;
let a = 0;
let b = 1;

print "First " + count + " Fibonacci numbers:";

while (count > 0) {
    print a;
    let next = a + b;
    a = b;
    b = next;
    count = count - 1;
}`,

    strings: `// Demonstrating String manipulation, concatenation, and equality checks
let greet = "Hello ";
let name = "Developer";
let message = greet + name + "!";

print message;

if (name == "Developer") {
    print "Equality check successful!";
} else {
    print "Mismatch.";
}

print "Concatenating strings and numbers: " + 100;`,

    scoping: `// Demonstrating Nested Block Scope Shadowing
let x = 10;
print "Global scope x: " + x; // Should print 10

{
    let x = 20;
    print "Inner block scope x: " + x; // Should print 20
    
    {
        let x = 30;
        print "Double nested block scope x: " + x; // Should print 30
    }
    
    print "Back to inner block scope x: " + x; // Should print 20
}

print "Back to global scope x: " + x; // Should print 10`,

    arrays: `// Demonstrating arrays, indexing, modifications, and len() built-in function
let arr = [10, 20, 30];
print "Original Array: " + arr;
print "Length of array: " + len(arr);
print "Element at index 1: " + arr[1];

arr[1] = 99;
print "Modified Array: " + arr;
print "Element at index 1: " + arr[1];

// String length
let text = "Hello Mini Compiler";
print "Length of '" + text + "': " + len(text);`,

    newfeatures: `/*
  Demonstrating the newly added language features:
  1. Multi-line comments
  2. Modulo operator (%)
  3. Float/Double values
  4. C-style 'for' loop (desugared)
  5. Built-in functions: type() and append()
*/

print "=== 1. Floats & Modulo ===";
let pi = 3.14159;
let r = 5.0;
let area = pi * r * r;
print "Area of circle: " + area;
print "5.5 % 2.0 = " + (5.5 % 2.0);

print "=== 2. For Loop ===";
for (let i = 0; i < 5; i = i + 1) {
    print "Iteration " + i;
}

print "=== 3. Built-in Functions ===";
let list = [1, 2];
print "Original: " + list;
print "Type of list: " + type(list);
append(list, 99.9);
print "Appended: " + list;
print "Type of 99.9: " + type(99.9);`
};

// Sync Line Numbers & Status Bar cursor tracker
function updateLineNumbers() {
    const text = editor.value;
    const lines = text.split('\n').length;
    let numberHtml = '';
    for (let i = 1; i <= lines; i++) {
        numberHtml += `<div>${i}</div>`;
    }
    lineNumbers.innerHTML = numberHtml;
}

function updateCursorPos() {
    const text = editor.value;
    const selStart = editor.selectionStart;
    const lines = text.substring(0, selStart).split('\n');
    const line = lines.length;
    const col = lines[lines.length - 1].length + 1;
    document.getElementById('status-line-col').innerText = `Ln ${line}, Col ${col}`;
    document.getElementById('editor-char-count').innerText = `${text.length} chars`;
}

// Autocomplete Snippet Definitions
const snippets = {
    'if': {
        code: `if (condition) {\n    \n} else {\n    \n}`,
        preview: 'if (condition) { ... } else { ... }',
        desc: 'Conditional branch statement'
    },
    'while': {
        code: `while (condition) {\n    \n}`,
        preview: 'while (condition) { ... }',
        desc: 'Conditional loop statement'
    },
    'for': {
        code: `for (let i = 0; i < 10; i = i + 1) {\n    \n}`,
        preview: 'for (let i = 0; i < 10; i = i + 1) { ... }',
        desc: 'Incremental iteration loop'
    },
    'fun': {
        code: `fun name(arg) {\n    \n}`,
        preview: 'fun name(arg) { ... }',
        desc: 'Function declaration'
    },
    'let': {
        code: `let x = 10;`,
        preview: 'let x = value;',
        desc: 'Variable declaration assignment'
    },
    'print': {
        code: `print "hello";`,
        preview: 'print expr;',
        desc: 'Output console print statement'
    }
};

const autocompletePopup = document.getElementById('autocomplete-popup');
const autocompletePreview = document.getElementById('autocomplete-preview');
const autocompleteDesc = document.getElementById('autocomplete-desc');
let activeSnippet = null;

function checkAutocomplete() {
    const text = editor.value;
    const selStart = editor.selectionStart;
    
    // Get text before cursor
    const beforeCursor = text.substring(0, selStart);
    
    // Find the word right before the cursor
    const words = beforeCursor.split(/[\s(){}[\];,+\-*/%&|]/);
    const lastWord = words[words.length - 1];
    
    if (snippets[lastWord]) {
        activeSnippet = snippets[lastWord];
        autocompletePreview.innerText = activeSnippet.preview;
        autocompleteDesc.innerText = activeSnippet.desc;
        autocompletePopup.classList.add('visible');
    } else {
        activeSnippet = null;
        autocompletePopup.classList.remove('visible');
    }
}

editor.addEventListener('input', () => {
    updateLineNumbers();
    updateCursorPos();
    checkAutocomplete();
});

editor.addEventListener('keyup', () => {
    updateCursorPos();
    checkAutocomplete();
});

editor.addEventListener('click', () => {
    updateCursorPos();
    checkAutocomplete();
});

editor.addEventListener('keydown', (e) => {
    if (e.key === 'Tab' && activeSnippet) {
        e.preventDefault();
        
        const text = editor.value;
        const selStart = editor.selectionStart;
        const beforeCursor = text.substring(0, selStart);
        const afterCursor = text.substring(selStart);
        
        const words = beforeCursor.split(/[\s(){}[\];,+\-*/%&|]/);
        const lastWord = words[words.length - 1];
        const lastWordStart = selStart - lastWord.length;
        
        const prefix = text.substring(0, lastWordStart);
        const snippetText = activeSnippet.code;
        
        editor.value = prefix + snippetText + afterCursor;
        
        let newCursorPos = lastWordStart + snippetText.indexOf('(') + 1;
        if (snippetText.indexOf('(') === -1) {
            newCursorPos = lastWordStart + snippetText.indexOf('=') + 2;
        }
        if (newCursorPos < lastWordStart || newCursorPos > lastWordStart + snippetText.length) {
            newCursorPos = lastWordStart + snippetText.length;
        }
        
        editor.selectionStart = newCursorPos;
        editor.selectionEnd = newCursorPos;
        
        activeSnippet = null;
        autocompletePopup.classList.remove('visible');
        updateLineNumbers();
        updateCursorPos();
    } else if (e.key === 'Tab') {
        e.preventDefault();
        const start = editor.selectionStart;
        const end = editor.selectionEnd;
        editor.value = editor.value.substring(0, start) + "    " + editor.value.substring(end);
        editor.selectionStart = editor.selectionEnd = start + 4;
        updateLineNumbers();
        updateCursorPos();
        checkAutocomplete();
    }
});

editor.addEventListener('scroll', () => {
    lineNumbers.scrollTop = editor.scrollTop;
});

// Initial setups
updateLineNumbers();
updateCursorPos();

// Copy Code
copyBtn.addEventListener('click', async () => {
    try {
        await navigator.clipboard.writeText(editor.value);
        copyBtn.querySelector('span').innerText = 'Copied!';
        setTimeout(() => {
            copyBtn.querySelector('span').innerText = 'Copy Code';
        }, 1500);
    } catch (err) {
        console.error("Clipboard copy failed: ", err);
    }
});

// Load presets
examplesSelect.addEventListener('change', () => {
    const val = examplesSelect.value;
    if (presets[val]) {
        editor.value = presets[val];
        updateLineNumbers();
        updateCursorPos();
    }
});

// Theme Customization Toggle
themeSelect.addEventListener('change', () => {
    const theme = themeSelect.value;
    document.documentElement.className = theme;
});

// Download Code
downloadBtn.addEventListener('click', () => {
    const code = editor.value;
    const blob = new Blob([code], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'program.txt';
    a.click();
    URL.revokeObjectURL(url);
});

// Tabs switching
tabButtons.forEach(btn => {
    btn.addEventListener('click', () => {
        const targetTab = btn.getAttribute('data-tab');
        
        tabButtons.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        tabContents.forEach(content => {
            content.classList.remove('active');
            if (content.id === `tab-${targetTab}`) {
                content.classList.add('active');
            }
        });
    });
});

// Run Program API Call with Hacker Stage Logs
runBtn.addEventListener('click', async () => {
    const code = editor.value;
    
    // UI Loading state
    runBtn.disabled = true;
    runBtn.querySelector('span').innerText = 'Running...';
    terminalOutput.innerHTML = `
        <div class="system-line">> Initializing execution pipeline...</div>
        <div class="system-line">> [Stage 1/3] Lexical Scanning (Lexer)...</div>
    `;
    tokensList.innerHTML = `<div class="placeholder-text">Scanning tokens...</div>`;
    astContainer.innerHTML = `<div class="placeholder-text">Building syntax tree...</div>`;

    try {
        const response = await fetch('/api/run', {
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain; charset=UTF-8'
            },
            body: code
        });

        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status}`);
        }

        const data = await response.json();
        
        // Add artificial delay to give a premium cyber pipeline logging experience
        setTimeout(() => {
            terminalOutput.innerHTML += `<div class="system-line">> [Stage 1/3] Lexical Scanning (Lexer)... OK</div>`;
            terminalOutput.innerHTML += `<div class="system-line">> [Stage 2/3] Syntax Parsing (Parser)...</div>`;
            
            setTimeout(() => {
                terminalOutput.innerHTML += `<div class="system-line">> [Stage 2/3] Syntax Parsing (Parser)... OK</div>`;
                terminalOutput.innerHTML += `<div class="system-line">> [Stage 3/3] Interpreter VM Walker (Walk AST)...</div>`;
                
                setTimeout(() => {
                    renderResult(data);
                }, 150);
            }, 150);
        }, 150);

    } catch (err) {
        terminalOutput.innerHTML += `<div class="error-line">HTTP Connection Error: ${err.message}</div>`;
        tokensList.innerHTML = `<div class="placeholder-text">Failed to fetch tokens.</div>`;
        astContainer.innerHTML = `<div class="placeholder-text">Failed to parse AST.</div>`;
        runBtn.disabled = false;
        runBtn.querySelector('span').innerText = 'Run Code';
    }
});

// Render Results to UI
function renderResult(data) {
    runBtn.disabled = false;
    runBtn.querySelector('span').innerText = 'Run Code';

    if (data.success) {
        let outputLines = data.output.trim().split('\n');
        let html = `
            <div class="system-line">> [Stage 3/3] Interpreter VM Walker (Walk AST)... OK</div>
            <div class="system-line">> Output stream active:</div>
        `;
        if (data.output.trim() === '') {
            html += `<div class="system-line">(Program executed with no console print output)</div>`;
        } else {
            outputLines.forEach(line => {
                html += `<div class="stdout-line">${escapeHtml(line)}</div>`;
            });
        }
        terminalOutput.innerHTML = html;

        // Calculate Analytics Telemetry metrics
        calculateMetrics(data.ast, data.tokens);

        // Tokens
        renderTokens(data.tokens);

        // AST Tree
        renderAst(data.ast);

    } else {
        // Render compile/runtime error
        terminalOutput.innerHTML = `
            <div class="system-line">> Pipeline execution failed:</div>
            <div class="error-line">${escapeHtml(data.error)}</div>
        `;
        tokensList.innerHTML = `<div class="placeholder-text" style="color: var(--accent-rose)">Tokens scanning interrupted.</div>`;
        astContainer.innerHTML = `<div class="placeholder-text" style="color: var(--accent-rose)">Parsing failed.</div>`;
        resetMetrics();
    }
}

// Calculate telemetry metric counts recursively from AST
function calculateMetrics(ast, tokens) {
    let varCount = 0;
    let loopCount = 0;

    function walk(node) {
        if (!node) return;

        if (node.type === "VarDeclaration") {
            varCount++;
        }
        if (node.type === "WhileStatement" || node.type === "IfStatement") {
            loopCount++;
        }

        // Recurse child nodes
        if (node.value) walk(node.value);
        if (node.expression) walk(node.expression);
        if (node.condition) walk(node.condition);
        if (node.left) walk(node.left);
        if (node.right) walk(node.right);
        
        if (node.thenBranch) node.thenBranch.forEach(walk);
        if (node.elseBranch) node.elseBranch.forEach(walk);
        if (node.body) node.body.forEach(walk);
        if (node.statements) node.statements.forEach(walk);
        if (node.arguments) node.arguments.forEach(walk);
    }

    ast.forEach(walk);

    // Update DOM
    document.getElementById('metric-tokens').innerText = tokens.length;
    document.getElementById('metric-statements').innerText = ast.length;
    document.getElementById('metric-vars').innerText = varCount;
    document.getElementById('metric-loops').innerText = loopCount;

    // Progress bar updates
    document.getElementById('progress-tokens').style.width = `${Math.min(tokens.length * 1.5, 100)}%`;
    document.getElementById('progress-statements').style.width = `${Math.min(ast.length * 5, 100)}%`;
    document.getElementById('progress-vars').style.width = `${Math.min(varCount * 12.5, 100)}%`;
    document.getElementById('progress-loops').style.width = `${Math.min(loopCount * 25, 100)}%`;
}

function resetMetrics() {
    document.getElementById('metric-tokens').innerText = "0";
    document.getElementById('metric-statements').innerText = "0";
    document.getElementById('metric-vars').innerText = "0";
    document.getElementById('metric-loops').innerText = "0";

    document.getElementById('progress-tokens').style.width = "0%";
    document.getElementById('progress-statements').style.width = "0%";
    document.getElementById('progress-vars').style.width = "0%";
    document.getElementById('progress-loops').style.width = "0%";
}

// Token Color Badging Categorization
function getTokenTypeClass(type) {
    const keywords = ['PRINT', 'LET', 'IF', 'ELSE', 'WHILE', 'FUN', 'RETURN'];
    const operators = ['PLUS', 'MINUS', 'MUL', 'DIV', 'LT', 'GT', 'LE', 'GE', 'EQ', 'EQEQ', 'BANG_EQ', 'AND', 'OR', 'BANG'];
    const punctuations = ['LPAREN', 'RPAREN', 'LBRACE', 'RBRACE', 'LBRACKET', 'RBRACKET', 'SEMICOLON', 'COMMA'];

    if (keywords.includes(type)) return 'token-keyword';
    if (operators.includes(type)) return 'token-operator';
    if (punctuations.includes(type)) return 'token-punctuation';
    if (type === 'IDENTIFIER') return 'token-identifier';
    if (type === 'NUMBER') return 'token-number';
    if (type === 'STRING') return 'token-string';
    if (type === 'EOF') return 'token-eof';
    return '';
}

function renderTokens(tokens) {
    if (!tokens || tokens.length === 0) {
        tokensList.innerHTML = `<span class="placeholder-text">No tokens scanned.</span>`;
        return;
    }

    let html = '';
    tokens.forEach(tok => {
        const cls = getTokenTypeClass(tok.type);
        html += `
            <span class="token-badge ${cls}" title="Type: ${tok.type}">
                <span>${escapeHtml(tok.value || tok.type)}</span>
                <span class="token-type">${tok.type}</span>
            </span>
        `;
    });
    tokensList.innerHTML = html;
}

// AST Tree Recursive Renderer
function renderAst(ast) {
    if (!ast || ast.length === 0) {
        astContainer.innerHTML = `<span class="placeholder-text">AST tree empty.</span>`;
        return;
    }

    let html = '<div class="ast-tree">';
    ast.forEach(stmt => {
        html += renderAstNode(stmt);
    });
    html += '</div>';
    astContainer.innerHTML = html;
}

function renderAstNode(node, label = "") {
    if (!node) return "";

    const type = node.type || "Unknown";
    let valueInfo = "";
    let childrenHtml = "";

    // Extract values and recurse children depending on node types
    if (type === "NumberExpression") {
        valueInfo = `: <span class="ast-node-value">${node.value}</span>`;
    } else if (type === "StringExpression") {
        valueInfo = `: <span class="ast-node-value">"${escapeHtml(node.value)}"</span>`;
    } else if (type === "VariableExpression") {
        valueInfo = `: <span class="ast-node-value">${escapeHtml(node.name)}</span>`;
    } else if (type === "VarDeclaration") {
        valueInfo = `: <span class="ast-node-value">${escapeHtml(node.name)}</span>`;
        childrenHtml += renderAstNode(node.value, "initial value");
    } else if (type === "Assignment") {
        valueInfo = `: <span class="ast-node-value">${escapeHtml(node.name)}</span>`;
        childrenHtml += renderAstNode(node.value, "new value");
    } else if (type === "PrintStatement") {
        childrenHtml += renderAstNode(node.expression, "expr");
    } else if (type === "IfStatement") {
        childrenHtml += renderAstNode(node.condition, "condition");
        childrenHtml += renderAstStatementsList(node.thenBranch, "then branch");
        if (node.elseBranch && node.elseBranch.length > 0) {
            childrenHtml += renderAstStatementsList(node.elseBranch, "else branch");
        }
    } else if (type === "WhileStatement") {
        childrenHtml += renderAstNode(node.condition, "condition");
        childrenHtml += renderAstStatementsList(node.body, "body");
    } else if (type === "FunctionDeclaration") {
        valueInfo = `: <span class="ast-node-value">${escapeHtml(node.name)}(${(node.parameters || []).map(escapeHtml).join(", ")})</span>`;
        childrenHtml += renderAstStatementsList(node.body, "body");
    } else if (type === "ReturnStatement") {
        childrenHtml += renderAstNode(node.expression, "value");
    } else if (type === "CallStatement") {
        childrenHtml += renderAstNode(node.call, "call");
    } else if (type === "BlockStatement") {
        childrenHtml += renderAstStatementsList(node.statements, "block");
    } else if (type === "IndexAssignment") {
        childrenHtml += renderAstNode(node.target, "target");
        childrenHtml += renderAstNode(node.value, "value");
    } else if (type === "BinaryExpression") {
        valueInfo = `: <span class="ast-node-value">${escapeHtml(node.operator)}</span>`;
        childrenHtml += renderAstNode(node.left, "left");
        childrenHtml += renderAstNode(node.right, "right");
    } else if (type === "UnaryExpression") {
        valueInfo = `: <span class="ast-node-value">${escapeHtml(node.operator)}</span>`;
        childrenHtml += renderAstNode(node.expression, "operand");
    } else if (type === "ArrayExpression") {
        if (node.elements && node.elements.length > 0) {
            node.elements.forEach((elem, i) => {
                childrenHtml += renderAstNode(elem, `elem ${i}`);
            });
        }
    } else if (type === "IndexExpression") {
        childrenHtml += renderAstNode(node.target, "target");
        childrenHtml += renderAstNode(node.index, "index");
    } else if (type === "FunctionCall") {
        valueInfo = `: <span class="ast-node-value">${escapeHtml(node.name)}()</span>`;
        if (node.arguments && node.arguments.length > 0) {
            node.arguments.forEach((arg, i) => {
                childrenHtml += renderAstNode(arg, `arg ${i}`);
            });
        }
    }

    const hasChildren = (childrenHtml !== "");
    const toggleHtml = hasChildren ? `<span class="ast-node-toggle">▼</span>` : `<span class="ast-node-toggle" style="opacity: 0">▼</span>`;
    const labelHtml = label ? `<span class="ast-node-label">[${label}]</span> ` : "";

    return `
        <div class="ast-node">
            <div class="ast-node-header">
                ${toggleHtml}
                ${labelHtml}<span class="ast-node-type">${type}</span>${valueInfo}
            </div>
            ${childrenHtml ? `<div class="ast-node-children">${childrenHtml}</div>` : ""}
        </div>
    `;
}

function renderAstStatementsList(statements, label) {
    if (!statements || statements.length === 0) return "";
    let childrenHtml = "";
    statements.forEach(stmt => {
        childrenHtml += renderAstNode(stmt);
    });
    return `
        <div class="ast-node">
            <div class="ast-node-header">
                <span class="ast-node-toggle">▼</span>
                <span class="ast-node-label">[${label}]</span> <span class="ast-node-type">Statements List</span>
            </div>
            <div class="ast-node-children">${childrenHtml}</div>
        </div>
    `;
}

// AST Tree Collapsible Expand Click delegation
astContainer.addEventListener('click', (e) => {
    const header = e.target.closest('.ast-node-header');
    if (!header) return;
    const node = header.closest('.ast-node');
    if (node && node.querySelector('.ast-node-children')) {
        node.classList.toggle('collapsed');
    }
});

// Helpers
function escapeHtml(str) {
    if (!str) return '';
    return str
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
