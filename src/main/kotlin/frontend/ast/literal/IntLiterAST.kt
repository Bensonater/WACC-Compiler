package frontend.ast.literal

import backend.GenerateASTVisitor
import backend.instruction.Instruction
import frontend.SymbolTable
import frontend.ast.ExprAST
import frontend.ast.type.BaseType
import frontend.ast.type.BaseTypeAST
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

/**
 * AST node representing an integer literal.
 * The value can store both positive and negative list of digits as
 * it is of type Int.
 */
class IntLiterAST(val ctx: ParserRuleContext, val value: Int) : ExprAST(ctx) {

    override fun getType(symbolTable: SymbolTable): TypeAST {
        return BaseTypeAST(ctx, BaseType.INT)
    }

    override fun accept(visitor: GenerateASTVisitor): List<Instruction> {
        return visitor.visitIntLiterAST(this)
    }
}