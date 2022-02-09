package frontend.ast.statement

import frontend.SymbolTable
import frontend.ast.ASTNode
import frontend.ast.ExprAST
import frontend.ast.type.BaseType
import frontend.ast.type.BaseTypeAST
import org.antlr.v4.runtime.ParserRuleContext

class WhileAST(val ctx: ParserRuleContext, val expr: ExprAST, val stats: List<ASTNode>) : StatAST(ctx) {
    override var symbolTable = SymbolTable()

    override fun check(symbolTable: SymbolTable): Boolean {
        if (!expr.check(symbolTable)) {
            return false
        }
        val exprType = expr.getType(symbolTable)
        if (exprType !is BaseTypeAST || exprType.type != BaseType.BOOL) {
            return false
        }
        stats.forEach {
            if (!it.check(symbolTable)) {
                return false
            }
        }

        return true
    }
}