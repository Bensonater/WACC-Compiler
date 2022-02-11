package frontend.ast

import frontend.SymbolTable
import frontend.ast.type.BaseType
import frontend.ast.type.BaseTypeAST
import frontend.ast.type.TypeAST
import frontend.semanticErrorHandler
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

/**
 * AST node representing a binary operator expression.
 * Checks the type of left and right-hand side expressions match.
 * Checks expression type is compatible with each operator.
 */
class BinOpExprAST(val ctx: ParserRuleContext, val binOp: BinOp, val expr1: ExprAST, val expr2: ExprAST) :
    ExprAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        if (!expr1.check(symbolTable) || !expr2.check(symbolTable)) {
            return false
        }
        val expr1Type = expr1.getType(symbolTable)
        val expr2Type = expr2.getType(symbolTable)
        if (expr1Type != expr2Type) {
            semanticErrorHandler.typeMismatch(ctx, expr1Type.toString(), expr2Type.toString())
            return false
        }

        return when (binOp) {
            is IntBinOp -> checkInt(expr1Type)
            is CmpBinOp -> checkCmp(expr1Type)
            is BoolBinOp -> checkBool(expr1Type)
            else -> {true}
        }

    }

    private fun checkInt(type1: TypeAST?): Boolean {
        if (type1 !is BaseTypeAST || type1.type != BaseType.INT) {
            semanticErrorHandler.typeMismatch(ctx, BaseType.INT.toString(), type1.toString())
            return false
        }
        return true
    }

    private fun checkCmp(type1: TypeAST?): Boolean {
        if (binOp == CmpBinOp.LT || binOp == CmpBinOp.GT || binOp == CmpBinOp.LTE || binOp == CmpBinOp.GTE) {
            if (type1 !is BaseTypeAST || type1.type != BaseType.INT  && type1.type != BaseType.CHAR) {
                semanticErrorHandler.typeMismatch(ctx, "INT or CHAR", type1.toString())
                return false
            }
        }
        return true
    }

    private fun checkBool(type1: TypeAST?): Boolean {
        if (type1 !is BaseTypeAST || type1.type != BaseType.BOOL) {
            semanticErrorHandler.typeMismatch(ctx, BaseType.BOOL.toString(), type1.toString())
            return false
        }
        return true
    }

    override fun getType(symbolTable: SymbolTable): TypeAST {
        return if (binOp is IntBinOp)
            BaseTypeAST(ctx, BaseType.INT)
        else
            BaseTypeAST(ctx, BaseType.BOOL)
    }

}

