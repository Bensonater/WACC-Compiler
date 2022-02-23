package frontend.ast

import backend.GenerateASTVisitor
import backend.instruction.Instruction
import org.antlr.v4.runtime.ParserRuleContext
import frontend.SymbolTable
import frontend.ast.type.PairTypeAST
import frontend.ast.type.TypeAST

/**
 * AST node representing a new pair holding the first and second field.
 */
class NewPairAST (val ctx: ParserRuleContext, val fst: ExprAST, val snd: ExprAST) : ASTNode(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        return (fst.check(symbolTable) && snd.check(symbolTable))
    }

    override fun getType(symbolTable: SymbolTable): TypeAST {
        return PairTypeAST(ctx, fst.getType(symbolTable)!!, snd.getType(symbolTable)!!)
    }

    override fun accept(visitor: GenerateASTVisitor): List<Instruction> {
        return visitor.visitNewPairAST(this)
    }
}