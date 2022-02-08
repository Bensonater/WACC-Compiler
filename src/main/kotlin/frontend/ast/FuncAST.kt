package frontend.ast

import frontend.SymbolTable
import org.antlr.v4.runtime.ParserRuleContext

class FuncAST(ctx: ParserRuleContext, ident:IdentAST, paramList:List<ParamsAST>, stat: StatAST ) : ASTNode(ctx) {
    override var symbolTable = SymbolTable()

}
