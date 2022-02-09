package frontend.ast

import frontend.SymbolTable
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

class ParamAST(val ctx: ParserRuleContext, val type: TypeAST, val ident: IdentAST) : ASTNode(ctx) {

    override fun check(symbolTable: SymbolTable): Boolean {
        return type.check(symbolTable) and ident.check(symbolTable)
    }
}
