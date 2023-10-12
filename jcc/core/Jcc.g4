grammar Jcc;

program: statement* | EOF;

statement: assignment | output | printing | expression;

assignment: VAR NAME ASSIGN expression;

output: OUT expression;

/**
 * Printing.
 * We support only quoted strings. Quotes inside the string should be masked with the slash '\'.
 * Non-quoted strings, empty or multiline strings are not supported.
 */
printing: PRINT STRING;

expression
          : identifier                       # id
          | number                           # value
          | LPAREN expression RPAREN         # parenthesis
          | expression POWER expression      # power
          | expression MULDIV expression     # muldiv
          | expression PLUSMINUS expression  # plusminus
          | PLUSMINUS expression             # unary
          | sequence                         # array
          | map                              # mapping
          | reduce                           # reducing
          ;

identifier: NAME;

number: NUMBER;

sequence: LCURLY expression COMMA expression RCURLY;

lambda1: identifier ARROW expression;

lambda2: identifier identifier ARROW expression;

map: MAP LPAREN expression COMMA lambda1 RPAREN;

reduce: REDUCE LPAREN expression COMMA expression COMMA lambda2 RPAREN;

/**
 * Reserved Keywords
 */
PRINT: 'print';
OUT: 'out';
VAR: 'var';
MAP: 'map';
REDUCE: 'reduce';

// Operators
PLUSMINUS: ('+'|'-');
MULDIV: ('*'|'/');
POWER: '^';
ASSIGN: '=';
ARROW: '->';

// Parentheses
LPAREN: '(';
RPAREN: ')';

LCURLY: '{';
RCURLY: '}';

// Punctuation
COMMA: ',';
DOT: '.';

// Integers
fragment DIGIT: [0-9];
NUMBER: (DIGIT+[.]*DIGIT*|DIGIT*[.]*DIGIT+);

// Variable names
NAME: [a-zA-Z|_][a-zA-Z0-9|_]*;

// String literals
STRING: UNTERMINATED_STRING '"';
UNTERMINATED_STRING: '"' (~["\\\r\n] | '\\' (. | EOF))*;

// Ignore all white spaces
WS: [ \t\r\n]+ -> skip ;
