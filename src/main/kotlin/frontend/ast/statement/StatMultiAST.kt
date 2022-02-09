package frontend.ast.statement

import frontend.SymbolTable
import frontend.ast.ASTNode
import org.antlr.v4.runtime.ParserRuleContext

class StatMultiAST (ctx: ParserRuleContext, val stats: List<StatAST>) : StatAST(ctx)  {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        for (stat in stats) {
            if (!stat.check(symbolTable)) {
                return false
            }
        }
        return true
    }
}