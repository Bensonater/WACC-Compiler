parser grammar WACCParser;

options {
  tokenVocab=WACCLexer;
}

program: BEGIN func* stat END;

func: type IDENT L_PARENTHESIS paramList? R_PARENTHESIS IS stat END;

paramList: param (COMMA param)*;

param: type IDENT;

stat: SKIP_STAT                      #statSkip
| type IDENT ASSIGN assignRhs        #statDeclare
| assignLhs ASSIGN assignRhs         #statAssign
| READ assignLhs                     #statRead
| FREE expr                          #statSimple
| RETURN expr                        #statSimple
| EXIT expr                          #statSimple
| PRINT expr                         #statSimple
| PRINTLN expr                       #statSimple
| IF expr THEN stat ELSE stat ENDIF  #statIf
| WHILE expr DO stat ENDWHILE        #statWhile
| BEGIN stat END                     #statBegin
| stat SEMICOLON stat                #statMulti
;

assignLhs: IDENT
| arrayElem
| pairElem;

assignRhs: expr
| arrayLiter
| NEWPAIR L_PARENTHESIS expr COMMA expr R_PARENTHESIS
| pairElem
| CALL IDENT L_PARENTHESIS argList? R_PARENTHESIS;

argList: expr (COMMA expr)*;

pairElem: FST expr | SND expr;

type: baseType | type L_BRACKET R_BRACKET | pairType;

baseType: INT_T | BOOL_T | CHAR_T | STRING_T;

pairType: PAIR L_PARENTHESIS pairElemType COMMA pairElemType R_PARENTHESIS;

pairElemType: baseType | type L_BRACKET R_BRACKET | PAIR;

expr: (PLUS | MINUS)? INTEGER      #exprSingle
| boolLiter                        #exprSingle
| CHAR_LITER                       #exprSingle
| STR_LITER                        #exprSingle
| NULL                             #exprSingle
| IDENT                            #exprSingle
| arrayElem                        #exprSingle
| unaryOper expr                   #exprUnOp
| expr binaryOper1 expr            #exprBinOp
| expr binaryOper2 expr            #exprBinOp
| expr binaryOper3 expr            #exprBinOp
| expr binaryOper4 expr            #exprBinOp
| expr binaryOper5 expr            #exprBinOp
| expr binaryOper6 expr            #exprBinOp
| L_PARENTHESIS expr R_PARENTHESIS #exprBrackets
;

unaryOper: NOT | MINUS | LEN | ORD | CHR;

binaryOper1: MULT | DIV | MOD;

binaryOper2: PLUS | MINUS;

binaryOper3: GT | GTE | LT | LTE;

binaryOper4: EQ | NEQ;

binaryOper5: AND;

binaryOper6: OR;

arrayElem: IDENT (L_BRACKET expr R_BRACKET)+;

arrayLiter: L_BRACKET (expr (COMMA expr)* )? R_BRACKET;

boolLiter: TRUE | FALSE;