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

    protected fun throwError(error: String) {
        errors.add(error)
    }

    abstract fun errorText(ctx: ParserRuleContext): String

    protected fun addErrorWithContext (ctx: ParserRuleContext, errorMessage: String) {
        val error = errorText(ctx) + errorMessage
        throwError(error)
    }
}