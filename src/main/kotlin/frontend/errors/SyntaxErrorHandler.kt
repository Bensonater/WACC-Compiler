package frontend.errors

import org.antlr.v4.runtime.ParserRuleContext

class SyntaxErrorHandler : ErrorHandler() {

    fun missingExitOrReturnError(ctx: ParserRuleContext) {
        addErrorWithContext(ctx, "Function does not end with an exit or return statement")
    }

    fun intOverflowError(ctx: ParserRuleContext) {
        addErrorWithContext(ctx, "Int assignment overflow")
    }


}