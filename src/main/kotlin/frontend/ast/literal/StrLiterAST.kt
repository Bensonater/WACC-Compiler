package frontend.ast.literal

import frontend.ast.ExprAST
import org.antlr.v4.runtime.ParserRuleContext

class StrLiterAST(ctx: ParserRuleContext, val value: String) : ExprAST(ctx) {
}