package frontend.ast

import frontend.SymbolTable
import org.antlr.v4.runtime.ParserRuleContext

abstract class ASTNode(ctx: ParserRuleContext) {
    abstract var symbolTable: SymbolTable

    open fun check(symbolTable: SymbolTable): Boolean {
        return true
    }

}