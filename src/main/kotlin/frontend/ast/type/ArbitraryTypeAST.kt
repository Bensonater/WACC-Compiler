package frontend.ast.type

import org.antlr.v4.runtime.ParserRuleContext

/**
 * AST node representing a pair without types for its element.
 * It can either represent null, Pair (pair, pair) or the type of the empty array.
 */
class ArbitraryTypeAST(ctx: ParserRuleContext) : TypeAST(ctx)  {
    override fun equals(other: Any?): Boolean {
        return other is PairTypeAST || other is ArbitraryTypeAST
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun toString(): String {
        return "ArbitraryTypeAST"
    }
}