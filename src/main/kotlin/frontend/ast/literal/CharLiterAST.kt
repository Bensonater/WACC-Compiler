package frontend.ast.literal

import frontend.ast.ExprAST
import org.antlr.v4.runtime.ParserRuleContext

class CharLiterAST(ctx: ParserRuleContext, val value: Char) : ExprAST(ctx) {
}