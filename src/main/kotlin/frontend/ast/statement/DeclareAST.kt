package frontend.ast.statement

import frontend.SymbolTable
import frontend.ast.ASTNode
import frontend.ast.FuncAST
import frontend.ast.IdentAST
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

class DeclareAST(val ctx: ParserRuleContext, val type: TypeAST, val ident: IdentAST, val assignRhs: ASTNode) : StatAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        val identAST = symbolTable.get(ident.name)
        if (identAST != null && identAST !is FuncAST) {
            // Call semantic error "Variable $ident.name already exist in scope"
            return false
        }
        if (!assignRhs.check(symbolTable)) {
            return false
        }
        if (assignRhs.getType(symbolTable) != type) {
            // Call semantic error "Invalid type for Assign Rhs"
            return false
        }
        symbolTable.put(ident.name, this)
        return true
    }
}
