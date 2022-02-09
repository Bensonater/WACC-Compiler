package frontend.ast

import org.antlr.v4.runtime.ParserRuleContext
import frontend.SymbolTable
import frontend.ast.type.PairTypeAST
import frontend.ast.type.TypeAST

class NewPairAST (val ctx: ParserRuleContext, val fst: ExprAST, val snd: ExprAST) : ASTNode(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        return (fst.check(symbolTable) && snd.check(symbolTable))
    }

    override fun getType(symbolTable: SymbolTable): TypeAST? {
        return PairTypeAST(ctx, fst.getType(symbolTable)!!, snd.getType(symbolTable)!!)
    }
}