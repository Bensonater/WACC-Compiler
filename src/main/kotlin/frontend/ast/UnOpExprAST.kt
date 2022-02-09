package frontend.ast

import frontend.SymbolTable
import frontend.ast.literal.BoolLiterAST
import frontend.ast.literal.CharLiterAST
import frontend.ast.literal.IntLiterAST
import org.antlr.v4.runtime.ParserRuleContext

enum class UnOp {
    NOT,
    MINUS,
    LEN,
    ORD,
    CHR
}

class UnOpExprAST(ctx: ParserRuleContext, val unOp: UnOp, val expr: ExprAST) : ExprAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        val result = when (unOp) {
            UnOp.NOT -> expr is BoolLiterAST
            UnOp.MINUS -> expr is IntLiterAST
            UnOp.LEN -> expr is ArrayElemAST
            UnOp.ORD -> expr is CharLiterAST
            UnOp.CHR -> expr is IntLiterAST
            else -> {
                false
            }
        }
        if (!result) {
            // throw an error about incompatible type
        }
        return true
    }
}

