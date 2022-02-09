package frontend.ast

import org.antlr.v4.runtime.ParserRuleContext

interface BinOp

enum class IntBinOp : BinOp {
    PLUS,
    MINUS,
    MULT,
    DIV,
    MOD
}

enum class CmpBinOp : BinOp {
    GTE,
    GT,
    LTE,
    LT,
    EQ,
    NEQ
}

enum class BoolBinOp : BinOp {
    AND,
    OR
}

class BinOpExprAST (ctx: ParserRuleContext, val binOp: BinOp, val expr1: ExprAST, expr2: ExprAST) : ExprAST(ctx) {
}

