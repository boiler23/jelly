grammar Jcc;

parse
 : GREET NAME EOF
 ;

GREET : 'Hi' | 'Hello';
NAME  : [a-zA-Z]+;
SPACE : [ \t\r\n] -> skip;
