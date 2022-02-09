package frontend.ast

import org.antlr.v4.runtime.ParserRuleContext

class IdentAST(val ctx: ParserRuleContext, val name: String) : ASTNode(ctx) {

}
