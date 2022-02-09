package frontend.ast.type

import frontend.SymbolTable
import frontend.ast.ASTNode
import org.antlr.v4.runtime.ParserRuleContext

class PairTypeAST(ctx: ParserRuleContext, val typeFst: TypeAST, val typeSnd: TypeAST) : TypeAST(ctx)  {
    override fun check(symbolTable: SymbolTable): Boolean {
        return typeFst.check(symbolTable) && typeSnd.check(symbolTable)
    }
}