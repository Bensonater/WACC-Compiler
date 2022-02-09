package frontend.ast

import frontend.SymbolTable
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

class ParamAST(val ctx: ParserRuleContext, val type: TypeAST, val ident:IdentAST) : ASTNode(ctx) {
    override var symbolTable = SymbolTable()

    override fun check(symbolTable: SymbolTable): Boolean {
        // we need to check that all the types are in scope of the symbol table
        if (type.getType(symbolTable) == null) {
            // TODO: throw semantic error about return type not found in symbol table
        }
        return true
    }
}
