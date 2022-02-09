package frontend.errors

import org.antlr.v4.runtime.ParserRuleContext

class SyntaxErrorHandler {

    private val errors: MutableList<String> = mutableListOf()

    fun hasErrors(): Boolean = errors.isNotEmpty()

    fun printErrors() {
        for (error in errors) {
            println(error)
        }
    }

    fun missingExitOrReturnError(ctx: ParserRuleContext) {
        val error = syntaxErrorText(ctx) + "Function does not end with an exit or return statement"
        syntaxError(error)
    }

    fun intOverflowError(ctx: ParserRuleContext) {
        val error = syntaxErrorText(ctx) + "Int assignment overflow"
        syntaxError(error)
    }

    private fun syntaxError(error: String) {
        errors.add(error)
    }

    private fun syntaxErrorText(ctx: ParserRuleContext): String {
        return ("Syntax Error ($SYNTAX_ERROR_CODE)\n - At ${ctx.getStart().line}:${ctx.getStart().charPositionInLine} : ")
    }
}