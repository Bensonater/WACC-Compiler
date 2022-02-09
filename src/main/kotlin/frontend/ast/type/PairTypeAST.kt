package frontend.ast.type

import frontend.ast.ASTNode
import org.antlr.v4.runtime.ParserRuleContext

class PairTypeAST(ctx: ParserRuleContext, val typeFst: TypeAST, val typeSnd: TypeAST) : ASTNode(ctx)  {
}