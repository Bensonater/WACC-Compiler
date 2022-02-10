package frontend.errors

import org.antlr.v4.runtime.ParserRuleContext

abstract class ErrorHandler {

    protected val errors: MutableList<String> = mutableListOf()

    fun hasErrors(): Boolean = errors.isNotEmpty()

    fun printErrors() {
        for (error in errors) {
            println(error)
        }
    }

    private fun syntaxError(error: String) {
        errors.add(error)
    }

    private fun syntaxErrorText(ctx: ParserRuleContext): String {
        return ("Syntax Error ($SYNTAX_ERROR_CODE)\n - At ${ctx.getStart().line}:${ctx.getStart().charPositionInLine} : ")
    }

    protected fun addErrorWithContext (ctx: ParserRuleContext, errorMessage: String) {
        val error = syntaxErrorText(ctx) + errorMessage
        syntaxError(error)
    }
}