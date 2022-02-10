package frontend.ast.statement

import frontend.SymbolTable
import frontend.ast.*
import frontend.ast.type.ArrayTypeAST
import frontend.semanticErrorHandler
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
            semanticErrorHandler.typeMismatch(ctx, leftType!!.toString(), rightType!!.toString())
            return false
        }
        if (assignLhs is IdentAST && symbolTable.lookupAll(assignLhs.name) is FuncAST) {
            semanticErrorHandler.invalidAssignment(ctx, "value", "function")
            return false
        }
        if (assignRhs is IdentAST && symbolTable.lookupAll(assignRhs.name) is FuncAST) {
            semanticErrorHandler.invalidAssignment(ctx, "function", "variable")
            return false
        }
        return true
    }
}
