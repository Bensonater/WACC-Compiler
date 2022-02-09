package frontend.ast

import frontend.SymbolTable
import frontend.ast.type.ArrayTypeAST
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

class ArrayElemAST(val ctx: ParserRuleContext, val ident: IdentAST, val listOfIndex: List<ExprAST>) : ExprAST(ctx) {

    override fun getType(symbolTable: SymbolTable): TypeAST? {
        val typeAST = ident.getType(symbolTable) as ArrayTypeAST
        return if (typeAST.dimension > listOfIndex.size) {
            ArrayTypeAST(ctx, typeAST.type, typeAST.dimension - listOfIndex.size)
        } else {
            typeAST.type
        }
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        if (!ident.check(symbolTable)) {
            return false
        }

        val typeAST = ident.getType(symbolTable) as ArrayTypeAST
        listOfIndex.forEach {
            if (it.getType(symbolTable) != typeAST.type) {
                // raise an error here about invalid type
                return false
            }
        }
        return true
    }
}