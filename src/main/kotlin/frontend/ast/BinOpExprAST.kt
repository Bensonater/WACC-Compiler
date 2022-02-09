package frontend.ast

import frontend.SymbolTable
import frontend.ast.literal.BoolLiterAST
import frontend.ast.literal.CharLiterAST
import frontend.ast.literal.IntLiterAST
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

class BinOpExprAST(ctx: ParserRuleContext, val binOp: BinOp, val expr1: ExprAST, val expr2: ExprAST) : ExprAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        val result = when (binOp) {
            is IntBinOp -> checkInt()
            is CmpBinOp -> checkCmp()
            is BoolBinOp -> checkBool()
            else -> {
                false
            }
        }
        if (!result) {
            // TODO: Raise error here
        }
        return true
    }

    private fun checkInt(): Boolean {
        return (expr1 is IntLiterAST) and (expr2 is IntLiterAST)
    }

    private fun checkCmp(): Boolean {
        if ((binOp == CmpBinOp.EQ) or (binOp == CmpBinOp.NEQ)) {
            return true
        }
        return checkInt() or ((expr1 is CharLiterAST) and (expr2 is CharLiterAST))
    }

    private fun checkBool(): Boolean {
        return (expr1 is BoolLiterAST) and (expr2 is BoolLiterAST)
    }

}
