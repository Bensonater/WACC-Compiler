package frontend.ast.statement

import frontend.SymbolTable
import frontend.ast.ASTNode
import org.antlr.v4.runtime.ParserRuleContext

abstract class StatAST(ctx: ParserRuleContext) : ASTNode(ctx) {

}
