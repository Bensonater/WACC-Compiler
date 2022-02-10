package frontend.ast

import frontend.SymbolTable
import frontend.ast.type.ArrayTypeAST
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

class ArrayElemAST(val ctx: ParserRuleContext, val ident: IdentAST, val listOfIndex: List<ExprAST>) : ExprAST(ctx) {

    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        if (!ident.check(symbolTable)) {
            return false
        }
        val typeAST = ident.getType(symbolTable)
        if (typeAST !is ArrayTypeAST) {
            // Call semantic error "Array elem type isn't an array"
            return false
        }
        if (listOfIndex.size != typeAST.dimension) {
            // Call semantic error "Array index dimension doesn't match actual array dimension"
            return false
        }
        listOfIndex.forEach {
            if (!it.check(symbolTable)) {
                return false
            }
        }
        return true
    }

    override fun getType(symbolTable: SymbolTable): TypeAST {
        val typeAST = ident.getType(symbolTable) as ArrayTypeAST
        return if (typeAST.dimension > listOfIndex.size) {
            ArrayTypeAST(ctx, typeAST.type, typeAST.dimension - listOfIndex.size)
        } else {
            typeAST.type
        }
    }
}