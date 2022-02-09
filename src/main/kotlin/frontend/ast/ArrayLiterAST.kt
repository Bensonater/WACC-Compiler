package frontend.ast

import org.antlr.v4.runtime.ParserRuleContext

class ArrayLiterAST (ctx: ParserRuleContext, val vals: List<ExprAST>) : ASTNode(ctx) {
}