package frontend.ast

import antlr.WACCParser
import antlr.WACCParserBaseVisitor

class BuildAST: WACCParserBaseVisitor<AST>() {
    override fun visitProgram(ctx: WACCParser.ProgramContext?): AST {
        return super.visitProgram(ctx)
    }

    override fun visitFunc(ctx: WACCParser.FuncContext?): AST {
        return super.visitFunc(ctx)
    }

    override fun visitParamList(ctx: WACCParser.ParamListContext?): AST {
        return super.visitParamList(ctx)
    }

    override fun visitParam(ctx: WACCParser.ParamContext?): AST {
        return super.visitParam(ctx)
    }

    override fun visitStat(ctx: WACCParser.StatContext?): AST {
        return super.visitStat(ctx)
    }

    override fun visitAssignLhs(ctx: WACCParser.AssignLhsContext?): AST {
        return super.visitAssignLhs(ctx)
    }

    override fun visitAssignRhs(ctx: WACCParser.AssignRhsContext?): AST {
        return super.visitAssignRhs(ctx)
    }

    override fun visitArgList(ctx: WACCParser.ArgListContext?): AST {
        return super.visitArgList(ctx)
    }

    override fun visitPairElem(ctx: WACCParser.PairElemContext?): AST {
        return super.visitPairElem(ctx)
    }

    override fun visitType(ctx: WACCParser.TypeContext?): AST {
        return super.visitType(ctx)
    }

    override fun visitBaseType(ctx: WACCParser.BaseTypeContext?): AST {
        return super.visitBaseType(ctx)
    }

    override fun visitPairType(ctx: WACCParser.PairTypeContext?): AST {
        return super.visitPairType(ctx)
    }

    override fun visitPairElemType(ctx: WACCParser.PairElemTypeContext?): AST {
        return super.visitPairElemType(ctx)
    }

    override fun visitExprInt(ctx: WACCParser.ExprIntContext?): AST {
        return super.visitExprInt(ctx)
    }

    override fun visitExprBrackets(ctx: WACCParser.ExprBracketsContext?): AST {
        return super.visitExprBrackets(ctx)
    }

    override fun visitExprArrayElem(ctx: WACCParser.ExprArrayElemContext?): AST {
        return super.visitExprArrayElem(ctx)
    }

    override fun visitExprStr(ctx: WACCParser.ExprStrContext?): AST {
        return super.visitExprStr(ctx)
    }

    override fun visitExprAlphaNumericBinOp(ctx: WACCParser.ExprAlphaNumericBinOpContext?): AST {
        return super.visitExprAlphaNumericBinOp(ctx)
    }

    override fun visitExprChar(ctx: WACCParser.ExprCharContext?): AST {
        return super.visitExprChar(ctx)
    }

    override fun visitExprNull(ctx: WACCParser.ExprNullContext?): AST {
        return super.visitExprNull(ctx)
    }

    override fun visitExprBool(ctx: WACCParser.ExprBoolContext?): AST {
        return super.visitExprBool(ctx)
    }

    override fun visitExprIdent(ctx: WACCParser.ExprIdentContext?): AST {
        return super.visitExprIdent(ctx)
    }

    override fun visitExprNumericBinOp(ctx: WACCParser.ExprNumericBinOpContext?): AST {
        return super.visitExprNumericBinOp(ctx)
    }

    override fun visitExprBoolBinOp(ctx: WACCParser.ExprBoolBinOpContext?): AST {
        return super.visitExprBoolBinOp(ctx)
    }

    override fun visitExprAnyBinOp(ctx: WACCParser.ExprAnyBinOpContext?): AST {
        return super.visitExprAnyBinOp(ctx)
    }

    override fun visitExprUnOp(ctx: WACCParser.ExprUnOpContext?): AST {
        return super.visitExprUnOp(ctx)
    }

    override fun visitUnaryOper(ctx: WACCParser.UnaryOperContext?): AST {
        return super.visitUnaryOper(ctx)
    }

    override fun visitBinaryOper1(ctx: WACCParser.BinaryOper1Context?): AST {
        return super.visitBinaryOper1(ctx)
    }

    override fun visitBinaryOper2(ctx: WACCParser.BinaryOper2Context?): AST {
        return super.visitBinaryOper2(ctx)
    }

    override fun visitBinaryOper3(ctx: WACCParser.BinaryOper3Context?): AST {
        return super.visitBinaryOper3(ctx)
    }

    override fun visitBinaryOper4(ctx: WACCParser.BinaryOper4Context?): AST {
        return super.visitBinaryOper4(ctx)
    }

    override fun visitBinaryOper5(ctx: WACCParser.BinaryOper5Context?): AST {
        return super.visitBinaryOper5(ctx)
    }

    override fun visitBinaryOper6(ctx: WACCParser.BinaryOper6Context?): AST {
        return super.visitBinaryOper6(ctx)
    }

    override fun visitArrayElem(ctx: WACCParser.ArrayElemContext?): AST {
        return super.visitArrayElem(ctx)
    }

    override fun visitArrayLiter(ctx: WACCParser.ArrayLiterContext?): AST {
        return super.visitArrayLiter(ctx)
    }

    override fun visitBoolLiter(ctx: WACCParser.BoolLiterContext?): AST {
        return super.visitBoolLiter(ctx)
    }
}