package frontend.ast.statement

import backend.GenerateASTVisitor
import backend.instruction.Instruction
import frontend.SymbolTable
import frontend.ast.*
import frontend.semanticErrorHandler
import org.antlr.v4.runtime.ParserRuleContext

/**
 * AST node representing an assignment statement to an existing variable.
 * Checks that type of left and right-hand side matches.
 */
class AssignAST(val ctx: ParserRuleContext, val assignLhs: ASTNode, val assignRhs: ASTNode) : StatAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        if (!assignLhs.check(symbolTable) || !assignRhs.check(symbolTable)) {
            return false
        }
        val leftType = assignLhs.getType(symbolTable)
        val rightType = assignRhs.getType(symbolTable)
        if (leftType != rightType) {
            semanticErrorHandler.typeMismatch(ctx, leftType!!.toString(), rightType!!.toString())
            return false
        }
        if (assignLhs is IdentAST && symbolTable.lookupAll(assignLhs.name) is FuncAST) {
            semanticErrorHandler.invalidAssignment(ctx, rightType.toString(), "FUNCTION")
            return false
        }
        if (assignRhs is IdentAST && symbolTable.lookupAll(assignRhs.name) is FuncAST) {
            semanticErrorHandler.invalidAssignment(ctx, leftType.toString(), "FUNCTION")
            return false
        }
        return true
    }

    override fun accept(visitor: GenerateASTVisitor): List<Instruction> {
        return visitor.visitAssignAST(this)
    }
}
