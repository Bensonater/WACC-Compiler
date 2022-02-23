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
 * AST node representing a boolean literal; value is True or False.
 */
class BoolLiterAST  (val ctx: ParserRuleContext, val value: Boolean) : ExprAST(ctx) {
    override fun getType(symbolTable: SymbolTable): TypeAST {
        return BaseTypeAST(ctx, BaseType.BOOL)
    }

    override fun accept(visitor: GenerateASTVisitor): List<Instruction> {
        return visitor.visitBoolLiterAST(this)
    }
}