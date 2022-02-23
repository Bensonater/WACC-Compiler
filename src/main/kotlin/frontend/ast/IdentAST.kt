package frontend.ast

import backend.GenerateASTVisitor
import backend.instruction.Instruction
import org.antlr.v4.runtime.ParserRuleContext
import frontend.SymbolTable
import frontend.ast.statement.DeclareAST
import frontend.ast.type.ArrayTypeAST
import frontend.ast.type.PairTypeAST
import frontend.ast.type.TypeAST
import frontend.semanticErrorHandler

/**
 * AST node representing an identifier.
 * Checks the identifier is in scope.
 */
class IdentAST(val ctx: ParserRuleContext, val name: String) : ExprAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        if (symbolTable.identLookUp(name) == null) {
            semanticErrorHandler.invalidIdentifier(ctx, name)
            return false
        }
        return true
    }

    override fun getType(symbolTable: SymbolTable): TypeAST {
        return when (val type = symbolTable.lookupAll(name)) {
                is DeclareAST -> type.type
                is FuncAST -> type.type
                is ParamAST -> type.type
                is ArrayTypeAST -> type
                is PairTypeAST -> type
                else -> throw RuntimeException("Unknown type")
        }
    }

    override fun accept(visitor: GenerateASTVisitor): List<Instruction> {
        return visitor.visitIdentAST(this)
    }
}
