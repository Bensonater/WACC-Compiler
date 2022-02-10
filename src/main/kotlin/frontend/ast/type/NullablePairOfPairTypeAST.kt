package frontend.ast.type

import org.antlr.v4.runtime.ParserRuleContext

class NullablePairOfPairTypeAST(ctx: ParserRuleContext) : TypeAST(ctx)  {
    override fun equals(other: Any?): Boolean {
        return other is PairTypeAST || other is NullablePairOfPairTypeAST
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}