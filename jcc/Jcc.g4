grammar Jcc;

program: statement | program statement;

statement: assignment | output | printing;

assignment: VAR NAME ASSIGN expression;

output: OUT expression;

printing: PRINT STRING;

expression:
            identifier
          | number
          | expression OP expression
          | LPAREN expression RPAREN
          | sequence
          | map
          | reduce
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
OP: ('+'|'-'|'*'|'/'|'^');
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
NUMBER: [+|-]*(DIGIT+[.]*DIGIT*|DIGIT*[.]*DIGIT+);

// Variable names
NAME: [a-zA-Z|_][a-zA-Z0-9|_]*;

// String literals
STRING: UNTERMINATED_STRING '"';
UNTERMINATED_STRING: '"' (~["\\\r\n] | '\\' (. | EOF))*;

// Ignore all white spaces
WS: [ \t\r\n]+ -> skip ;
