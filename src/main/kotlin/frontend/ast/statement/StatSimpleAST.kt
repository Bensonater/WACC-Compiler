package frontend.ast.statement

import frontend.SymbolTable
import frontend.ast.ExprAST
import org.antlr.v4.runtime.ParserRuleContext

enum class Command {
    FREE, RETURN, EXIT, PRINT, PRINTLN
}

class StatSimpleAST(ctx: ParserRuleContext, command: Command, val expr: ExprAST) : StatAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        return expr.check(symbolTable)
    }
}
