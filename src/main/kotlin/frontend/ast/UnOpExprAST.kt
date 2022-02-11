package frontend.ast

import frontend.SymbolTable
import frontend.ast.type.ArrayTypeAST
import frontend.ast.type.BaseType
import frontend.ast.type.BaseTypeAST
import frontend.ast.type.TypeAST
import frontend.semanticErrorHandler
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
        this.symbolTable = symbolTable
        if (!expr.check(symbolTable)) {
            return false
        }
        if (unOp == UnOp.LEN) {
            if (expr.getType(symbolTable) !is ArrayTypeAST) {
                semanticErrorHandler.typeMismatch(ctx, "ARRAY", expr.getType(symbolTable).toString())
                return false
            }
            return true
        }
        val exprType = expr.getType(symbolTable)
        if (exprType !is BaseTypeAST) {
            return false
        }
        when (unOp) {
            UnOp.NOT -> {
                if (exprType.type != BaseType.BOOL) {
                    semanticErrorHandler.typeMismatch(ctx, BaseType.BOOL.toString(), exprType.toString())
                    return false
                }
            }
            UnOp.MINUS -> {
                if (exprType.type != BaseType.INT) {
                    semanticErrorHandler.typeMismatch(ctx, BaseType.INT.toString(), exprType.toString())
                    return false
                }
            }
            UnOp.ORD -> {
                if (exprType.type != BaseType.CHAR) {
                    semanticErrorHandler.typeMismatch(ctx, BaseType.CHAR.toString(), exprType.toString())
                    return false
                }
            }
            UnOp.CHR -> {
                if (exprType.type != BaseType.INT) {
                    semanticErrorHandler.typeMismatch(ctx, BaseType.INT.toString(), exprType.toString())
                    return false
                }
            }
            else -> {return false}
        }
        return true
    }

    override fun getType(symbolTable: SymbolTable): TypeAST {
        return when (unOp) {
            UnOp.NOT -> BaseTypeAST(ctx, BaseType.BOOL)
            UnOp.CHR -> BaseTypeAST(ctx, BaseType.CHAR)
            UnOp.MINUS, UnOp.LEN, UnOp.ORD -> BaseTypeAST(ctx, BaseType.INT)
        }
    }
}

