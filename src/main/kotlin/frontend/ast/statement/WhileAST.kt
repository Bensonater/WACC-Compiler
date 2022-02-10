package frontend.ast.statement

import frontend.SymbolTable
import frontend.ast.ASTNode
import frontend.ast.ExprAST
import frontend.ast.type.BaseType
import frontend.ast.type.BaseTypeAST
import org.antlr.v4.runtime.ParserRuleContext

class WhileAST(val ctx: ParserRuleContext, val expr: ExprAST, val stats: List<ASTNode>) : StatAST(ctx) {
    val bodySymbolTable = SymbolTable()

    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        bodySymbolTable.setParentTable(symbolTable)
        if (!expr.check(symbolTable)) {
            return false
        }
        val exprType = expr.getType(symbolTable)
        if (exprType !is BaseTypeAST || exprType.type != BaseType.BOOL) {
            // Call semantic error "While condition should be of type Bool"
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