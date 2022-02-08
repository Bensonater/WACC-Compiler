package frontend.ast.statement

import frontend.ast.ExprAST
import org.antlr.v4.runtime.ParserRuleContext

enum class Command {
    FREE, RETURN, EXIT, PRINT, PRINTLN
}

class StatSimpleAST(ctx: ParserRuleContext, command: Command, expr: ExprAST) : StatAST(ctx) {

}
