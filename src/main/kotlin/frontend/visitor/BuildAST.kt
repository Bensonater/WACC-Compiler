package frontend.visitor

import antlr.WACCParser
import antlr.WACCParserBaseVisitor
import frontend.ast.*

class BuildAST: WACCParserBaseVisitor<ASTNode>() {
    override fun visitProgram(ctx: WACCParser.ProgramContext): ASTNode {
        val funcList = mutableListOf<FuncAST>()
        for (func in ctx.func()) {
            funcList.add(visit(func) as FuncAST)
        }
        val stat = visit(ctx.stat()) as StatAST
        return ProgramAST(ctx, funcList, stat)
    }

    override fun visitFunc(ctx: WACCParser.FuncContext): ASTNode {
        val paramList = mutableListOf<ParamAST>()
        for (param in ctx.paramList().param()) {
            paramList.add(visit(param) as ParamAST)
        }
        val ident = visit(ctx.IDENT()) as IdentAST
        val stat = visit(ctx.stat()) as StatAST
        return FuncAST(ctx, ident, paramList, stat)
    }

    override fun visitParam(ctx: WACCParser.ParamContext): ASTNode {
        return ParamAST(ctx, visit(ctx.type()) as TypeAST, visit(ctx.IDENT()) as IdentAST)
    }

    override fun visitAssignLhs(ctx: WACCParser.AssignLhsContext): ASTNode {
        return super.visitAssignLhs(ctx)
    }

    override fun visitAssignRhs(ctx: WACCParser.AssignRhsContext): ASTNode {
        return super.visitAssignRhs(ctx)
    }

    override fun visitArgList(ctx: WACCParser.ArgListContext): ASTNode {
        return super.visitArgList(ctx)
    }

    override fun visitPairElem(ctx: WACCParser.PairElemContext): ASTNode {
        return super.visitPairElem(ctx)
    }

    override fun visitType(ctx: WACCParser.TypeContext): ASTNode {
        return super.visitType(ctx)
    }

    override fun visitBaseType(ctx: WACCParser.BaseTypeContext): ASTNode {
        return super.visitBaseType(ctx)
    }

    override fun visitPairType(ctx: WACCParser.PairTypeContext): ASTNode {
        return super.visitPairType(ctx)
    }

    override fun visitPairElemType(ctx: WACCParser.PairElemTypeContext): ASTNode {
        return super.visitPairElemType(ctx)
    }

    override fun visitExprInt(ctx: WACCParser.ExprIntContext): ASTNode {
        return super.visitExprInt(ctx)
    }

    override fun visitExprBrackets(ctx: WACCParser.ExprBracketsContext): ASTNode {
        return super.visitExprBrackets(ctx)
    }

    override fun visitExprArrayElem(ctx: WACCParser.ExprArrayElemContext): ASTNode {
        return super.visitExprArrayElem(ctx)
    }

    override fun visitExprStr(ctx: WACCParser.ExprStrContext): ASTNode {
        return super.visitExprStr(ctx)
    }

    override fun visitExprAlphaNumericBinOp(ctx: WACCParser.ExprAlphaNumericBinOpContext): ASTNode {
        return super.visitExprAlphaNumericBinOp(ctx)
    }

    override fun visitExprChar(ctx: WACCParser.ExprCharContext): ASTNode {
        return super.visitExprChar(ctx)
    }

    override fun visitExprNull(ctx: WACCParser.ExprNullContext): ASTNode {
        return super.visitExprNull(ctx)
    }

    override fun visitExprBool(ctx: WACCParser.ExprBoolContext): ASTNode {
        return super.visitExprBool(ctx)
    }

    override fun visitExprIdent(ctx: WACCParser.ExprIdentContext): ASTNode {
        return super.visitExprIdent(ctx)
    }

    override fun visitExprNumericBinOp(ctx: WACCParser.ExprNumericBinOpContext): ASTNode {
        return super.visitExprNumericBinOp(ctx)
    }

    override fun visitExprBoolBinOp(ctx: WACCParser.ExprBoolBinOpContext): ASTNode {
        return super.visitExprBoolBinOp(ctx)
    }

    override fun visitExprAnyBinOp(ctx: WACCParser.ExprAnyBinOpContext): ASTNode {
        return super.visitExprAnyBinOp(ctx)
    }

    override fun visitExprUnOp(ctx: WACCParser.ExprUnOpContext): ASTNode {
        return super.visitExprUnOp(ctx)
    }

    override fun visitUnaryOper(ctx: WACCParser.UnaryOperContext): ASTNode {
        return super.visitUnaryOper(ctx)
    }

    override fun visitBinaryOper1(ctx: WACCParser.BinaryOper1Context): ASTNode {
        return super.visitBinaryOper1(ctx)
    }

    override fun visitBinaryOper2(ctx: WACCParser.BinaryOper2Context): ASTNode {
        return super.visitBinaryOper2(ctx)
    }

    override fun visitBinaryOper3(ctx: WACCParser.BinaryOper3Context): ASTNode {
        return super.visitBinaryOper3(ctx)
    }

    override fun visitBinaryOper4(ctx: WACCParser.BinaryOper4Context): ASTNode {
        return super.visitBinaryOper4(ctx)
    }

    override fun visitBinaryOper5(ctx: WACCParser.BinaryOper5Context): ASTNode {
        return super.visitBinaryOper5(ctx)
    }

    override fun visitBinaryOper6(ctx: WACCParser.BinaryOper6Context): ASTNode {
        return super.visitBinaryOper6(ctx)
    }

    override fun visitArrayElem(ctx: WACCParser.ArrayElemContext): ASTNode {
        return super.visitArrayElem(ctx)
    }

    override fun visitArrayLiter(ctx: WACCParser.ArrayLiterContext): ASTNode {
        return super.visitArrayLiter(ctx)
    }

    override fun visitBoolLiter(ctx: WACCParser.BoolLiterContext): ASTNode {
        return super.visitBoolLiter(ctx)
    }

    override fun visitReadStat(ctx: WACCParser.ReadStatContext): ASTNode {
        return super.visitReadStat(ctx)
    }

    override fun visitIfStat(ctx: WACCParser.IfStatContext): ASTNode {
        return super.visitIfStat(ctx)
    }

    override fun visitBeginStat(ctx: WACCParser.BeginStatContext): ASTNode {
        return super.visitBeginStat(ctx)
    }

    override fun visitMultiStat(ctx: WACCParser.MultiStatContext): ASTNode {
        return super.visitMultiStat(ctx)
    }

    override fun visitSkipStat(ctx: WACCParser.SkipStatContext): ASTNode {
        return super.visitSkipStat(ctx)
    }

    override fun visitSingleStat(ctx: WACCParser.SingleStatContext): ASTNode {
        return super.visitSingleStat(ctx)
    }

    override fun visitAssignStat(ctx: WACCParser.AssignStatContext): ASTNode {
        return super.visitAssignStat(ctx)
    }

    override fun visitDeclareStat(ctx: WACCParser.DeclareStatContext): ASTNode {
        return super.visitDeclareStat(ctx)
    }

    override fun visitWhileStat(ctx: WACCParser.WhileStatContext): ASTNode {
        return super.visitWhileStat(ctx)
    }
}