package frontend.ast

import org.antlr.v4.runtime.ParserRuleContext
import frontend.SymbolTable
import frontend.ast.statement.DeclareAST
import frontend.ast.type.ArrayTypeAST
import frontend.ast.type.PairTypeAST
import frontend.ast.type.TypeAST

class IdentAST(val ctx: ParserRuleContext, val name: String) : ASTNode(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        if (symbolTable.lookupAll(name) == null) {
            // Return semantic error "Variable is not assigned {name}"
            return false
        }
        return true
    }

    override fun getType(symbolTable: SymbolTable): TypeAST? {
        val type = symbolTable.identLookUp(name)
        if (type == null) {
            // Semantic error, Variable $name has not been declared
            return null
        } else {
            return when (type) {
                is DeclareAST -> type.type
                is FuncAST -> type.type
                is ParamAST -> type.type
                is ArrayTypeAST -> type
                is PairTypeAST -> type
                else -> null // throw RuntimeException("Unknown class implementing Identifiable")
            }
        }
    }
}
