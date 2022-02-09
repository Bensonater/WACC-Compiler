package frontend.ast

import frontend.SymbolTable
import frontend.ast.statement.StatAST
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

class FuncAST(val ctx: ParserRuleContext, val type: TypeAST, val ident:IdentAST, val paramList:List<ParamAST>, val stats: List<StatAST>) : ASTNode(ctx) {
    override var symbolTable = SymbolTable()

    override fun check(symbolTable: SymbolTable): Boolean {
        paramList.forEach { it.check(symbolTable) }
        ident.check(symbolTable)
        stats.forEach { it.check(symbolTable) }
        return true
    }
}
