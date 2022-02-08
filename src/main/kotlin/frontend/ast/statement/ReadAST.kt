package frontend.ast.statement

import frontend.ast.ASTNode
import org.antlr.v4.runtime.ParserRuleContext

class ReadAST(ctx: ParserRuleContext, assignLhs: ASTNode) : StatAST(ctx) {

}
