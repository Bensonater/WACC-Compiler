package frontend.ast.statement

import frontend.ast.ASTNode
import frontend.ast.IdentAST
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

class DeclareAST(ctx: ParserRuleContext, type: TypeAST, ident: IdentAST, assignRhs: ASTNode) : StatAST(ctx) {

}
