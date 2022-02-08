package frontend.ast.statement

import frontend.ast.ASTNode
import frontend.ast.ExprAST
import frontend.ast.IdentAST
import org.antlr.v4.runtime.ParserRuleContext

class CallAST(ctx: ParserRuleContext, ident: IdentAST, paramList: List<ExprAST>) : StatAST(ctx) {
}