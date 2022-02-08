package frontend.ast

import frontend.SymbolTable
import frontend.ast.statement.StatAST
import org.antlr.v4.runtime.ParserRuleContext

class FuncAST(ctx: ParserRuleContext, ident:IdentAST, paramList:List<ParamAST>, stat: StatAST) : ASTNode(ctx) {
    override var symbolTable = SymbolTable()

}
