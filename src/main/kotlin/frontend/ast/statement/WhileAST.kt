package frontend.ast.statement

import frontend.ast.ASTNode
import frontend.ast.ExprAST
import org.antlr.v4.runtime.ParserRuleContext

class WhileAST(ctx: ParserRuleContext, expr: ExprAST, stats: List<ASTNode>) : StatAST(ctx)  {
}