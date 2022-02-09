package frontend.ast

import org.antlr.v4.runtime.ParserRuleContext

enum class UnOp {
    NOT,
    MINUS,
    LEN,
    ORD,
    CHR
}

class UnOpExprAST(ctx: ParserRuleContext, val unOp: UnOp, val expr: ExprAST) : ExprAST(ctx) {
}

