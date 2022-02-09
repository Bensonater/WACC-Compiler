package frontend.ast

import frontend.SymbolTable
import frontend.ast.type.PairTypeAST
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

enum class PairIndex {
    FST,
    SND
}

class PairElemAST (ctx: ParserRuleContext, val index: PairIndex, val expr: ExprAST) : ASTNode(ctx) {
    override fun getType(symbolTable: SymbolTable): TypeAST? {
        val elemType = expr.getType(symbolTable)
        return if (elemType is PairTypeAST) {
            when (index) {
                PairIndex.FST -> elemType.typeFst
                PairIndex.SND -> elemType.typeSnd
            }
        } else {
            // semanticError("Expected type PAIR, Actual type $pairType", ctx)
            null
        }

    }

}

