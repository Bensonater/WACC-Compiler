package frontend.ast.statement

import frontend.ast.ASTNode
import frontend.ast.ExprAST
import org.antlr.v4.runtime.ParserRuleContext

class IfAST(ctx: ParserRuleContext, expr: ExprAST, trueStat: StatAST, elseStat: StatAST) : StatAST(ctx) {

}
