package frontend.visitor

import antlr.WACCParser.*
import antlr.WACCParserBaseVisitor
import frontend.ast.ASTNode

class SyntaxChecker: WACCParserBaseVisitor<Void>()  {

    private fun statIsExitOrReturn(stat: StatContext) = (stat is StatSimpleContext) &&
            (stat.EXIT() != null || stat.RETURN() != null)

    private fun ifStatEndsWithExitOrReturn(stat: StatIfContext): Boolean {

        var ifLastStat = stat.stat(0)
        if (ifLastStat is StatMultiContext) {
            ifLastStat = ifLastStat.stat().last()
        }
        var doesEndWithExitOrReturn = statIsExitOrReturn(ifLastStat)
        if (!doesEndWithExitOrReturn && ifLastStat is StatIfContext) {
            doesEndWithExitOrReturn = ifStatEndsWithExitOrReturn(ifLastStat)
        }

        var elseLastStat = stat.stat(1)
        if (elseLastStat is StatMultiContext) {
            elseLastStat = elseLastStat.stat().last()
        }
        if (doesEndWithExitOrReturn) {
            doesEndWithExitOrReturn = if (elseLastStat is StatIfContext) {
                ifStatEndsWithExitOrReturn(elseLastStat)
            } else {
                statIsExitOrReturn(elseLastStat)
            }
        }

        return doesEndWithExitOrReturn
    }

    override fun visitFunc(ctx: FuncContext): Void? {
        var funcStat = ctx.stat()
        if (funcStat is StatMultiContext) {
            funcStat = funcStat.stat().last()
        }
        var endsWithExitOrReturn = statIsExitOrReturn(funcStat)

        if (funcStat is StatIfContext) {
            endsWithExitOrReturn = ifStatEndsWithExitOrReturn(funcStat)
        }

        if (!endsWithExitOrReturn) {
           // syntaxError("Function does not end with an exit or return statement", ctx)
        }
        return null
    }

    override fun visitIntLiter(ctx: IntLiterContext): Void? {
        try {
            (ctx.text).toInt()
        } catch (e: NumberFormatException) {
//            syntaxError("int out of bound", ctx)
        }
        return null
    }

}