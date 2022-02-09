package frontend.ast

import org.antlr.v4.runtime.ParserRuleContext

class ArrayElemAST (ctx: ParserRuleContext, val ident: IdentAST, val listOfIndex: List<ExprAST>) : ExprAST(ctx) {
}