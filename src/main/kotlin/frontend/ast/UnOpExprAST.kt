package frontend.ast

import frontend.SymbolTable
import frontend.ast.type.BaseType
import frontend.ast.type.BaseTypeAST
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

enum class UnOp {
    NOT,
    MINUS,
    LEN,
    ORD,
    CHR
}

class UnOpExprAST(val ctx: ParserRuleContext, val unOp: UnOp, val expr: ExprAST) : ExprAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        if (!expr.check(symbolTable)) {
            return false
        }
        if (unOp == UnOp.LEN) {
            return expr is ArrayElemAST
        }

        val exprType = expr.getType(symbolTable)
        if (exprType !is BaseTypeAST) {
            return false
        }
        return when (unOp) {
            UnOp.NOT -> exprType.type == BaseType.BOOL
            UnOp.MINUS -> exprType.type == BaseType.INT
            UnOp.ORD -> exprType.type == BaseType.CHAR
            UnOp.CHR -> exprType.type == BaseType.INT
            else -> {
                false
            }
        }
    }

    override fun getType(symbolTable: SymbolTable): TypeAST? {
        return when (unOp) {
            UnOp.NOT -> BaseTypeAST(ctx, BaseType.BOOL)
            UnOp.CHR -> BaseTypeAST(ctx, BaseType.CHAR)
            UnOp.MINUS, UnOp.LEN, UnOp.ORD -> BaseTypeAST(ctx, BaseType.INT)
        }
    }
}

