package frontend.ast.statement

import frontend.ast.ASTNode
import frontend.ast.IdentAST
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

class DeclareAST(val ctx: ParserRuleContext, val type: TypeAST, val ident: IdentAST, val assignRhs: ASTNode) : StatAST(ctx) {

}
