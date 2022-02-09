package frontend.ast

import org.antlr.v4.runtime.ParserRuleContext

class PairTypeAST(ctx: ParserRuleContext, val typeFst: TypeAST, val typeSnd: TypeAST) : ASTNode(ctx)  {
}