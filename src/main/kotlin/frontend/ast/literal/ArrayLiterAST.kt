package frontend.ast.literal

import frontend.ast.ASTNode
import frontend.ast.ExprAST
import org.antlr.v4.runtime.ParserRuleContext

class ArrayLiterAST (ctx: ParserRuleContext, val vals: List<ExprAST>) : ASTNode(ctx) {
}