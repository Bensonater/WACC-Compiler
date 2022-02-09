package frontend.ast.literal

import frontend.ast.ExprAST
import org.antlr.v4.runtime.ParserRuleContext

class IntLiterAST(ctx: ParserRuleContext, val value: Int) : ExprAST(ctx) {
}