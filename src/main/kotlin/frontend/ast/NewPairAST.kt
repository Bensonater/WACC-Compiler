package frontend.ast

import org.antlr.v4.runtime.ParserRuleContext
import frontend.SymbolTable

class NewPairAST (val ctx: ParserRuleContext, val fst: ExprAST, val snd: ExprAST) : ASTNode(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        return (fst.check(symbolTable) && snd.check(symbolTable))
    }
}