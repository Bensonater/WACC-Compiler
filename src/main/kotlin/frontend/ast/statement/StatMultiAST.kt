package frontend.ast.statement

import frontend.ast.ASTNode
import org.antlr.v4.runtime.ParserRuleContext

class StatMultiAST (ctx: ParserRuleContext, val stats:MutableList<ASTNode>) : StatAST(ctx)  {
}