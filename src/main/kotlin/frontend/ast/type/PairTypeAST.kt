package frontend.ast.type

import frontend.SymbolTable
import frontend.ast.ASTNode
import org.antlr.v4.runtime.ParserRuleContext

class PairTypeAST(ctx: ParserRuleContext, val typeFst: TypeAST, val typeSnd: TypeAST) : TypeAST(ctx)  {
    override fun check(symbolTable: SymbolTable): Boolean {
        return typeFst.check(symbolTable) && typeSnd.check(symbolTable)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PairTypeAST

        if (typeFst != other.typeFst) return false
        if (typeSnd != other.typeSnd) return false

        return true
    }

    override fun hashCode(): Int {
        var result = typeFst.hashCode()
        result = 31 * result + typeSnd.hashCode()
        return result
    }
}