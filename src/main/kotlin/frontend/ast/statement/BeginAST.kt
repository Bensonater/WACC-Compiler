package frontend.ast.statement

import frontend.SymbolTable
import org.antlr.v4.runtime.ParserRuleContext

class BeginAST(ctx: ParserRuleContext, val stats: List<StatAST>) : StatAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = SymbolTable()
        this.symbolTable.parent = symbolTable
        for (stat in stats) {
            if (!stat.check(this.symbolTable)) {
                return false
            }
        }
        return true
    }
}
