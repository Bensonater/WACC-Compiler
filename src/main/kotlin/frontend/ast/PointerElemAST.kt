package frontend.ast

import backend.ASTVisitor
import frontend.SymbolTable
import frontend.ast.type.PointerTypeAST
import frontend.ast.type.TypeAST
import frontend.semanticErrorHandler
import org.antlr.v4.runtime.ParserRuleContext

class PointerElemAST(val ctx: ParserRuleContext, val ident: IdentAST): ASTNode(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        if (!ident.check(symbolTable)) {
            return false
        }
        val identType = ident.getType(symbolTable)
        if (identType !is PointerTypeAST) {
            semanticErrorHandler.typeMismatch(ctx, "Pointer", identType.toString())
            return false
        }
        return true
    }

    override fun getType(symbolTable: SymbolTable): TypeAST {
        return (ident.getType(symbolTable) as PointerTypeAST).type
    }

    override fun <S : T, T> accept(visitor: ASTVisitor<S>): T? {
        return visitor.visitPointerElemAST(this)
    }
}