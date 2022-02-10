package frontend.ast.statement

import frontend.SymbolTable
import frontend.ast.ASTNode
import frontend.ast.ExprAST
import frontend.ast.type.BaseType
import frontend.ast.type.BaseTypeAST
import frontend.semanticErrorHandler
import org.antlr.v4.runtime.ParserRuleContext

class WhileAST(val ctx: ParserRuleContext, val expr: ExprAST, val stats: List<ASTNode>) : StatAST(ctx) {
    val bodySymbolTable = SymbolTable()

    override fun check(symbolTable: SymbolTable): Boolean {
        bodySymbolTable.setParentTable(symbolTable)
        if (!expr.check(symbolTable)) {
            return false
        }
        val exprType = expr.getType(symbolTable)
        if (exprType !is BaseTypeAST || exprType.type != BaseType.BOOL) {
            semanticErrorHandler.invalidConditional(ctx)
            return false
        }
        stats.forEach {
            if (!it.check(bodySymbolTable)) {
                return false
            }
        }
        return true
    }
}