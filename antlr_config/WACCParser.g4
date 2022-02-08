parser grammar WACCParser;

options {
  tokenVocab=WACCLexer;
}

program: BEGIN func* stat END;

func: type IDENT L_PARENTHESIS paramList? R_PARENTHESIS IS stat END;

paramList: param (COMMA param)*;

param: type IDENT;

stat: SKIP_STAT                      #skipStat
| type IDENT ASSIGN assignRhs        #declareStat
| assignLhs ASSIGN assignRhs         #assignStat
| READ assignLhs                     #readStat
| FREE expr                          #singleStat
| RETURN expr                        #singleStat
| EXIT expr                          #singleStat
| PRINT expr                         #singleStat
| PRINTLN expr                       #singleStat
| IF expr THEN stat ELSE stat ENDIF  #ifStat
| WHILE expr DO stat ENDWHILE        #whileStat
| BEGIN stat END                     #beginStat
| stat SEMICOLON stat                #multiStat
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

expr: INTEGER #exprInt
| boolLiter   #exprBool
| CHAR_LITER  #exprChar
| STR_LITER   #exprStr
| NULL        #exprNull
| IDENT       #exprIdent
| arrayElem   #exprArrayElem
| unaryOper expr         #exprUnOp
| expr binaryOper1 expr  #exprNumericBinOp
| expr binaryOper2 expr  #exprNumericBinOp
| expr binaryOper3 expr  #exprAlphaNumericBinOp
| expr binaryOper4 expr  #exprAnyBinOp
| expr binaryOper5 expr  #exprBoolBinOp
| expr binaryOper6 expr  #exprBoolBinOp
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