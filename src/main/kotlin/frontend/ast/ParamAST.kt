package frontend.ast

import frontend.SymbolTable
import org.antlr.v4.runtime.ParserRuleContext

class ParamAST(ctx: ParserRuleContext, type:TypeAST, ident:IdentAST) : ASTNode(ctx) {
    override var symbolTable = SymbolTable()
}
