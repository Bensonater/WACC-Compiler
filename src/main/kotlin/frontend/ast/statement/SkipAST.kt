package frontend.ast.statement

import backend.GenerateASTVisitor
import backend.instruction.Instruction
import org.antlr.v4.runtime.ParserRuleContext

/**
 * AST node representing a skip statement
 */
class SkipAST(ctx: ParserRuleContext) : StatAST(ctx) {

    override fun accept(visitor: GenerateASTVisitor): List<Instruction> {
        return visitor.visitSkipAST(this)
    }
}
