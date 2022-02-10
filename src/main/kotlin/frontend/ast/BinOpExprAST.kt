package frontend.ast

import frontend.SymbolTable
import frontend.ast.type.BaseType
import frontend.ast.type.BaseTypeAST
import frontend.ast.type.TypeAST
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

class BinOpExprAST(val ctx: ParserRuleContext, val binOp: BinOp, val expr1: ExprAST, val expr2: ExprAST) :
    ExprAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        if (!expr1.check(symbolTable) || !expr2.check(symbolTable)) {
            return false
        }
        val expr1Type = expr1.getType(symbolTable)
        val expr2Type = expr2.getType(symbolTable)
        if (expr1Type !is BaseTypeAST || expr2Type !is BaseTypeAST) {
            return false
        }

        return when (binOp) {
            is IntBinOp -> checkInt(expr1Type, expr2Type)
            is CmpBinOp -> checkCmp(expr1Type, expr2Type)
            is BoolBinOp -> checkBool(expr1Type, expr2Type)
            else -> {
                false
            }
        }

    }

    private fun checkInt(type1: BaseTypeAST, type2: BaseTypeAST): Boolean {
        return type1.type == BaseType.INT && type2.type == BaseType.INT
    }

    private fun checkCmp(type1: BaseTypeAST, type2: BaseTypeAST): Boolean {
        if (binOp == CmpBinOp.EQ || binOp == CmpBinOp.NEQ) {
            return type1.type == type2.type
        }
        return checkInt(type1, type2) || (type1.type == BaseType.CHAR && type2.type == BaseType.CHAR)
    }

    private fun checkBool(type1: BaseTypeAST, type2: BaseTypeAST): Boolean {
        return type1.type == BaseType.BOOL && type2.type == BaseType.BOOL
    }

    override fun getType(symbolTable: SymbolTable): TypeAST? {
        return if (binOp is IntBinOp)
            BaseTypeAST(ctx, BaseType.INT)
        else
            BaseTypeAST(ctx, BaseType.BOOL)
    }

}

