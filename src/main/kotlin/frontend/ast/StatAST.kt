package frontend.ast

import frontend.SymbolTable
import org.antlr.v4.runtime.ParserRuleContext

class StatAST(ctx: ParserRuleContext) : ASTNode(ctx) {
    override var symbolTable = SymbolTable()

}
