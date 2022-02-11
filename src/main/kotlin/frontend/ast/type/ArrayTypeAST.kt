package frontend.ast.type

import frontend.SymbolTable
import org.antlr.v4.runtime.ParserRuleContext
import java.util.*

class ArrayTypeAST(ctx: ParserRuleContext, val type: TypeAST, val dimension: Int) : TypeAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        return type.check(symbolTable)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArrayTypeAST

        if (type is NullablePairOfPairTypeAST || other.type is NullablePairOfPairTypeAST) return true
        if (type != other.type) return false
        if (dimension != other.dimension) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(type, dimension)
    }

    override fun toString(): String {
        val arrayString = StringBuilder()
        arrayString.append(type.toString())
        for (i in 1..dimension) {
            arrayString.append("[]")
        }
        return arrayString.toString()
    }
}