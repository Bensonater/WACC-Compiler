package frontend.ast.statement

import frontend.SymbolTable
import frontend.ast.*
import frontend.ast.type.ArrayTypeAST
import org.antlr.v4.runtime.ParserRuleContext

class AssignAST(val ctx: ParserRuleContext, val assignLhs: ASTNode, val assignRhs: ASTNode) : StatAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        if (!assignLhs.check(symbolTable) || !assignRhs.check(symbolTable)) {
            return false
        }
        var leftType = assignLhs.getType(symbolTable)
        val rightType = assignRhs.getType(symbolTable)
        if (leftType is ArrayTypeAST) {
            leftType = leftType.type
        }
        if (leftType != rightType) {
            // Call semantic error "Assign Lhs and Rhs type mismatch"
            return false
        }
        if (assignLhs is IdentAST && symbolTable.lookupAll(assignLhs.name) is FuncAST) {
            // Call semantic error "Error assigning value to function"
            return false
        }
        if (assignRhs is IdentAST && symbolTable.lookupAll(assignRhs.name) is FuncAST) {
            // Call semantic error "Error assigning function to variable"
            return false
        }
        return true
    }
}
