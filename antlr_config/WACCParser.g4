parser grammar WACCParser;

options {
  tokenVocab=WACCLexer;
}

program: BEGIN func* stat END;

func: type ident L_PARENTHESIS paramList? R_PARENTHESIS IS stat END;

paramList: param (COMMA param)*;

param: type ident;

stat: SKIP_STAT
| type ident ASSIGN assignRhs
| assignLhs ASSIGN assignRhs
| READ assignLhs
| FREE expr
| RETURN expr
| EXIT expr
| PRINT expr
| PRINTLN expr
| IF expr THEN stat ELSE stat ENDIF
| WHILE expr DO stat ENDWHILE
| BEGIN stat END
| stat SEMICOLON stat;

assignLhs: ident
| arrayElem
| pairElem;

assignRhs: expr
| arrayLiter
| NEWPAIR L_PARENTHESIS expr COMMA expr R_PARENTHESIS
| pairElem
| CALL ident L_PARENTHESIS argList? R_PARENTHESIS;

argList: expr (COMMA expr)*;

pairElem: FST expr | SND expr;

type: baseType | type L_BRACKET R_BRACKET | pairType;

baseType: INT_T | BOOL_T | CHAR_T | STRING_T;

pairType: PAIR L_PARENTHESIS pairElemType COMMA pairElemType R_PARENTHESIS;

pairElemType: baseType | type L_BRACKET R_BRACKET | PAIR;

expr: INTEGER
| boolLiter
| CHAR_LITER
| STR_LITER
| NULL
| ident
| arrayElem
| unaryOper expr
| expr binaryOper expr
| L_PARENTHESIS expr R_PARENTHESIS;

unaryOper: NOT | MINUS | LEN | ORD | CHR;

binaryOper: MULT | DIV | MOD | PLUS | MINUS | GT | GTE | LT | LTE
| EQ | NEQ | AND | OR;

ident: (UNDERSCORE | ALPHA) (UNDERSCORE | ALPHA | DIGIT)*;

arrayElem: ident (L_BRACKET expr R_BRACKET)+;

arrayLiter: L_BRACKET (expr (COMMA expr)* )? R_BRACKET;

boolLiter: TRUE | FALSE;