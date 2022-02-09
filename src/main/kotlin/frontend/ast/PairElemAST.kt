package frontend.ast

import org.antlr.v4.runtime.ParserRuleContext

enum class PairIndex {
    FST,
    SND
}

class PairElemAST (ctx: ParserRuleContext, val index: PairIndex, val expr: ExprAST) : ASTNode(ctx) {
}

