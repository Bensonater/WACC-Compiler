package frontend.ast

import org.antlr.v4.runtime.ParserRuleContext

class IdentAST(val ctx: ParserRuleContext, val name: String) : ASTNode(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        if (symbolTable.get(name) == null) {
            // Return semantic error "Variable is not assigned {name}"
            return false
        }
        return true
    }
}
