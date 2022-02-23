package frontend.ast

import backend.GenerateASTVisitor
import backend.instruction.Instruction
import frontend.SymbolTable
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

/**
 * AST node representing a parameter with a type and identifier.
 */
class ParamAST(val ctx: ParserRuleContext, val type: TypeAST, val ident: IdentAST) : ASTNode(ctx) {

    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        return type.check(symbolTable) && ident.check(symbolTable)
    }

    override fun accept(visitor: GenerateASTVisitor): List<Instruction> {
        return visitor.visitParamAST(this)
    }
}
