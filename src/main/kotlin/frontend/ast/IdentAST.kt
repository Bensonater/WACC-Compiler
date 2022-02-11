package frontend.ast

import org.antlr.v4.runtime.ParserRuleContext
import frontend.SymbolTable
import frontend.ast.statement.DeclareAST
import frontend.ast.type.ArrayTypeAST
import frontend.ast.type.PairTypeAST
import frontend.ast.type.TypeAST
import frontend.semanticErrorHandler

class IdentAST(val ctx: ParserRuleContext, val name: String) : ExprAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        if (symbolTable.identLookUp(name) == null) {
            semanticErrorHandler.invalidIdentifier(ctx, name)
            return false
        }
        return true
    }

    override fun getType(symbolTable: SymbolTable): TypeAST? {
        val type = symbolTable.identLookUp(name)
        return if (type == null) {
            semanticErrorHandler.invalidIdentifier(ctx, name)
            null
        } else {
            when (type) {
                is DeclareAST -> type.type
                is FuncAST -> type.type
                is ParamAST -> type.type
                is ArrayTypeAST -> type
                is PairTypeAST -> type
                else -> throw RuntimeException("Unknown type")
            }
        }
    }
}
