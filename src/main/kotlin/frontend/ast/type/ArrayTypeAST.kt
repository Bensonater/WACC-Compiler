package frontend.ast.type

import frontend.ast.ASTNode
import org.antlr.v4.runtime.ParserRuleContext

class ArrayTypeAST(ctx: ParserRuleContext, val type: TypeAST, val dimension: Int) : ASTNode(ctx)  {
}