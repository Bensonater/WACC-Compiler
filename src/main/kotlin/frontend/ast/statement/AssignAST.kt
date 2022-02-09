package frontend.ast.statement

import frontend.ast.ASTNode
import org.antlr.v4.runtime.ParserRuleContext

class AssignAST(ctx: ParserRuleContext, assignLhs: ASTNode, assignRhs: ASTNode) : StatAST(ctx) {

}
