package frontend.ast

import frontend.SymbolTable
import frontend.ast.statement.StatAST
import org.antlr.v4.runtime.ParserRuleContext

class FuncAST(val ctx: ParserRuleContext, val ident:IdentAST, val paramList:List<ParamAST>, val stats: List<StatAST>) : ASTNode(ctx) {
    override var symbolTable = SymbolTable()

}
