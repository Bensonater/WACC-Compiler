package frontend.ast

import org.antlr.v4.runtime.ParserRuleContext

class CallAST(ctx: ParserRuleContext, ident:IdentAST, paramList:List<ExprAST>) : ASTNode(ctx) {
}