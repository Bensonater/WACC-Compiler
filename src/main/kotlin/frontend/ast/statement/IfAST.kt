package frontend.ast.statement

import frontend.SymbolTable
import frontend.ast.ExprAST
import frontend.ast.type.BaseType
import frontend.ast.type.BaseTypeAST
import org.antlr.v4.runtime.ParserRuleContext

class IfAST(val ctx: ParserRuleContext, val expr: ExprAST, val thenStat: List<StatAST>, val elseStat: List<StatAST>) :
    StatAST(ctx) {
    val thenSymbolTable = SymbolTable()
    val elseSymbolTable = SymbolTable()

    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        thenSymbolTable.setParentTable(symbolTable)
        elseSymbolTable.setParentTable(symbolTable)
        if (!expr.check(symbolTable)) {
            return false
        }
        val exprType = expr.getType(symbolTable)
        if (exprType != BaseTypeAST(ctx, BaseType.BOOL)) {
            // Call semantic error "If condition should be of type Bool"
            return false
        }
        thenStat.forEach {
            if (!it.check(thenSymbolTable)) {
                return false
            }
        }
        elseStat.forEach {
            if (!it.check(elseSymbolTable)) {
                return false
            }
        }
        return true
    }
}
