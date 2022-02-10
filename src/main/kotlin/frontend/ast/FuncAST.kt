package frontend.ast

import frontend.FuncSymbolTable
import frontend.SymbolTable
import frontend.ast.statement.StatAST
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

class FuncAST(
    val ctx: ParserRuleContext,
    val type: TypeAST,
    val ident: IdentAST,
    val paramList: List<ParamAST>,
    val stats: List<StatAST>
) : ASTNode(ctx) {
    init {
        symbolTable = FuncSymbolTable(type)
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable.setParentTable(symbolTable)
        paramList.forEach {
            if (!it.check(symbolTable)) {
                return false
            }
        }

        stats.forEach {
            if (!it.check(symbolTable)) {
                return false
            }
        }
        return ident.check(symbolTable)
    }
}
