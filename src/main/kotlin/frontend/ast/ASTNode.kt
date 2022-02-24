package frontend.ast

import backend.GenerateASTVisitor
import backend.instruction.Instruction
import frontend.SymbolTable
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

/**
 * Abstract AST node encapsulating all AST nodes
 */
abstract class ASTNode(ctx: ParserRuleContext) {
    open var symbolTable = SymbolTable()

    open fun check(symbolTable: SymbolTable): Boolean {
        return true
    }

    open fun getType(symbolTable: SymbolTable): TypeAST? {
        return null
    }

    open fun accept(visitor : GenerateASTVisitor): List<Instruction>? {
        return null
    }

}