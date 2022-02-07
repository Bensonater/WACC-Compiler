package frontend

import antlr.*

class Visitor : WACCParserBaseVisitor<Void>() {
    override fun visitProgram(ctx: WACCParser.ProgramContext?): Void {
        return super.visitProgram(ctx)
    }

    override fun visitFunc(ctx: WACCParser.FuncContext?): Void {
        return super.visitFunc(ctx)
    }

    override fun visitParamList(ctx: WACCParser.ParamListContext?): Void {
        return super.visitParamList(ctx)
    }

    override fun visitParam(ctx: WACCParser.ParamContext?): Void {
        return super.visitParam(ctx)
    }

    override fun visitStat(ctx: WACCParser.StatContext?): Void {
        return super.visitStat(ctx)
    }

    override fun visitAssignLhs(ctx: WACCParser.AssignLhsContext?): Void {
        return super.visitAssignLhs(ctx)
    }

    override fun visitAssignRhs(ctx: WACCParser.AssignRhsContext?): Void {
        return super.visitAssignRhs(ctx)
    }

    override fun visitArgList(ctx: WACCParser.ArgListContext?): Void {
        return super.visitArgList(ctx)
    }

    override fun visitPairElem(ctx: WACCParser.PairElemContext?): Void {
        return super.visitPairElem(ctx)
    }

    override fun visitType(ctx: WACCParser.TypeContext?): Void {
        return super.visitType(ctx)
    }

    override fun visitBaseType(ctx: WACCParser.BaseTypeContext?): Void {
        return super.visitBaseType(ctx)
    }

    override fun visitPairType(ctx: WACCParser.PairTypeContext?): Void {
        return super.visitPairType(ctx)
    }

    override fun visitPairElemType(ctx: WACCParser.PairElemTypeContext?): Void {
        return super.visitPairElemType(ctx)
    }

    override fun visitExprInt(ctx: WACCParser.ExprIntContext?): Void {
        return super.visitExprInt(ctx)
    }

    override fun visitExprBrackets(ctx: WACCParser.ExprBracketsContext?): Void {
        return super.visitExprBrackets(ctx)
    }

    override fun visitExprArrayElem(ctx: WACCParser.ExprArrayElemContext?): Void {
        return super.visitExprArrayElem(ctx)
    }

    override fun visitExprStr(ctx: WACCParser.ExprStrContext?): Void {
        return super.visitExprStr(ctx)
    }

    override fun visitExprAlphaNumericBinOp(ctx: WACCParser.ExprAlphaNumericBinOpContext?): Void {
        return super.visitExprAlphaNumericBinOp(ctx)
    }

    override fun visitExprChar(ctx: WACCParser.ExprCharContext?): Void {
        return super.visitExprChar(ctx)
    }

    override fun visitExprNull(ctx: WACCParser.ExprNullContext?): Void {
        return super.visitExprNull(ctx)
    }

    override fun visitExprBool(ctx: WACCParser.ExprBoolContext?): Void {
        return super.visitExprBool(ctx)
    }

    override fun visitExprIdent(ctx: WACCParser.ExprIdentContext?): Void {
        return super.visitExprIdent(ctx)
    }

    override fun visitExprNumericBinOp(ctx: WACCParser.ExprNumericBinOpContext): Void? {
        print(ctx.expr(0).text)
        return super.visitExprNumericBinOp(ctx)
    }

    override fun visitExprBoolBinOp(ctx: WACCParser.ExprBoolBinOpContext?): Void {
        return super.visitExprBoolBinOp(ctx)
    }

    override fun visitExprAnyBinOp(ctx: WACCParser.ExprAnyBinOpContext?): Void {
        return super.visitExprAnyBinOp(ctx)
    }

    override fun visitExprUnOp(ctx: WACCParser.ExprUnOpContext?): Void {
        return super.visitExprUnOp(ctx)
    }

    override fun visitUnaryOper(ctx: WACCParser.UnaryOperContext?): Void {
        return super.visitUnaryOper(ctx)
    }

    override fun visitArrayElem(ctx: WACCParser.ArrayElemContext?): Void {
        return super.visitArrayElem(ctx)
    }

    override fun visitArrayLiter(ctx: WACCParser.ArrayLiterContext?): Void {
        return super.visitArrayLiter(ctx)
    }

    override fun visitBoolLiter(ctx: WACCParser.BoolLiterContext?): Void {
        return super.visitBoolLiter(ctx)
    }
}