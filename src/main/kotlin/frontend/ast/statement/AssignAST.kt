package frontend.ast.statement

import frontend.SymbolTable
import frontend.ast.*
import frontend.ast.literal.ArrayLiterAST
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

class AssignAST(val ctx: ParserRuleContext, val assignLhs: ASTNode, val assignRhs: ASTNode) : StatAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        if (!assignLhs.check(symbolTable) || !assignRhs.check(symbolTable)) {
            return false
        }
        var leftType = assignLhs.getType(symbolTable)
        val rightType = assignRhs.getType(symbolTable)
        if (leftType !is IdentAST && leftType !is ArrayElemAST && leftType !is PairElemAST) {
            // Return semantic error "Assign Lhs type is not valid"
        }
        if (rightType !is ExprAST && rightType !is ArrayLiterAST &&
            rightType !is NewPairAST && rightType !is PairElemAST && rightType !is CallAST) {
            // Return semantic error "Assign Rhs type is not valid"
        }
        if (leftType is ArrayElemAST) {
            leftType = leftType.type
        }
        if (leftType != rightType) {
            // Return semantic error "Assign Lhs and Rhs type mismatch"
        }
        return true
    }
}
