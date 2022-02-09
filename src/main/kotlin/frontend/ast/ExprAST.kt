package frontend.ast

import org.antlr.v4.runtime.ParserRuleContext

abstract class ExprAST(ctx: ParserRuleContext) : ASTNode(ctx) {
}