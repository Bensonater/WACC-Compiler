lexer grammar WACCLexer;

// Comments

COMMENT: '#' [^EOL]* -> skip;

// Whitespace

WS : [ \t]+ -> skip;

// EOL

EOL: '\n';

// Binary Operators

MULT: '*';
DIV: '/';
MOD: '%';
PLUS: '+';
MINUS: '-';
AND: '&&';
OR: '||';

// Unary Operators

NOT: '!';
LEN: 'len';
ORD: 'ord';
CHR: 'chr';

// Comparators

GT: '>';
GTE: '>=';
LT: '<';
LTE: '<=';
EQ: '==';
NEQ: '!=';

// Brackets

L_PARENTHESES: '(';
R_PARENTHESES: ')';
L_SQ_BRACKETS: '[';
R_SQ_BRACKETS: ']';

// Numbers

DIGIT: '0'..'9';
fragment INT_SIGN: PLUS | MINUS;
INTEGER: INT_SIGN? DIGIT+;

// Boolean

TRUE: 'true';
FALSE: 'false';

// Characters

fragment CHAR: [^\\\'"];
fragment ESC_CHAR: [0|b|t|n|f|r|"|\'|\\];
fragment CHARACTER: CHAR | '\\' ESC_CHAR;
ALPHA: [a-zA-Z];
UNDERSCORE: '_';

// String literal

STR_LITER: '"' CHARACTER* '"';

// Character literal

CHAR_LITER: '\'' CHARACTER '\'';

// Pairs

NULL: 'null';
PAIR: 'pair';
COMMA: ',';
FST: 'fst';
SND: 'snd';
NEWPAIR: 'newpair';

// Base types

INT_T: 'int';
BOOL_T: 'bool';
CHAR_T: 'char';
STRING_T: 'string';

// Functions

CALL: 'call';
IS: 'is';

// Statements

SKIP: 'skip';
ASSIGN: '=';
READ: 'read';
FREE: 'free';
RETURN: 'return';
EXIT: 'exit';
PRINT: 'print';
PRINTLN: 'println';
IF: 'if';
THEN: 'then';
ELSE: 'else';
ENDIF: 'fi';
WHILE: 'while';
DO: 'do';
ENDWHILE: 'done';
BEGIN: 'begin';
END: 'end';
SEMICOLON: ';';
