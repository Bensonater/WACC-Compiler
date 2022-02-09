package frontend.ast.statement

import frontend.SymbolTable
import frontend.ast.*
import frontend.ast.literal.ArrayLiterAST
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
        if (assignLhs !is IdentAST && assignLhs !is ArrayElemAST && assignLhs !is PairElemAST) {
            // Call semantic error "Assign Lhs type is not valid"
            return false
        }
        if (assignRhs !is ExprAST && assignRhs !is ArrayLiterAST &&
            assignRhs !is NewPairAST && assignRhs !is PairElemAST && assignRhs !is CallAST) {
            // Call semantic error "Assign Rhs type is not valid"
            return false
        }
        if (leftType is ArrayTypeAST) {
            leftType = leftType.type
        }
        if (leftType != rightType) {
            // Call semantic error "Assign Lhs and Rhs type mismatch"
            return false
        }
        return true
    }
}
