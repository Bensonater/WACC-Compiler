package frontend.ast

import org.antlr.v4.runtime.ParserRuleContext

class NewPairAST (val ctx: ParserRuleContext, val fst: ExprAST, val snd: ExprAST) : ASTNode(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        return (fst.check(symbolTable) && snd.check(symbolTable))
    }
}