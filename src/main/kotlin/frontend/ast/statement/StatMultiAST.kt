package frontend.ast.statement

import backend.GenerateASTVisitor
import backend.instruction.Instruction
import frontend.SymbolTable
import org.antlr.v4.runtime.ParserRuleContext

/**
 * AST node representing multi-statements.
 */
class StatMultiAST (ctx: ParserRuleContext, val stats: List<StatAST>) : StatAST(ctx)  {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        for (stat in stats) {
            if (!stat.check(symbolTable)) {
                return false
            }
        }
        return true
    }

    override fun accept(visitor: GenerateASTVisitor): List<Instruction> {
        return visitor.visitStatMultiAST(this)
    }
}