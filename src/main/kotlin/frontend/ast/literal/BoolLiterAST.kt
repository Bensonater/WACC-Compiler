package frontend.ast.literal

import frontend.ast.ExprAST
import org.antlr.v4.runtime.ParserRuleContext

class BoolLiterAST  (ctx: ParserRuleContext, val value: Boolean) : ExprAST(ctx) {
}