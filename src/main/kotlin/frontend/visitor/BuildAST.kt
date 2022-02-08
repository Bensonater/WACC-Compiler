package frontend.visitor

import antlr.WACCParser
import antlr.WACCParserBaseVisitor
import frontend.ast.*
import frontend.ast.statement.CallAST
import frontend.ast.statement.StatAST
import frontend.ast.statement.StatMultiAST
import frontend.ast.statement.WhileAST

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
        if (ctx.paramList() != null) {
            for (param in ctx.paramList().param()) {
                paramList.add(visit(param) as ParamAST)
            }
        }
        val ident = visit(ctx.IDENT()) as IdentAST
        val stat = visit(ctx.stat()) as StatAST
        return if (stat is StatMultiAST) {
            FuncAST(ctx, ident, paramList, stat.stats)
        } else {
            FuncAST(ctx, ident, paramList, mutableListOf(stat))
        }
    }

    override fun visitParam(ctx: WACCParser.ParamContext): ASTNode {
        return ParamAST(ctx, visit(ctx.type()) as TypeAST, visit(ctx.IDENT()) as IdentAST)
    }

    override fun visitAssignRhs(ctx: WACCParser.AssignRhsContext): ASTNode {
        if (ctx.NEWPAIR() != null) {
            return NewPairAST(ctx,
                visit(ctx.expr(0)) as ExprAST,
                visit(ctx.expr(1)) as ExprAST
            )
        }
        if (ctx.CALL() != null) {
            val argList = mutableListOf<ExprAST>()
            if (ctx.argList() != null) {
                for (expr in ctx.argList().expr()) {
                    argList.add(visit(expr) as ExprAST)
                }
            }
            return CallAST(ctx, visit(ctx.IDENT()) as IdentAST, argList)
        }
        return visitChildren(ctx)
    }

    override fun visitStatWhile(ctx: WACCParser.StatWhileContext): ASTNode {
        val ctxStat = visit(ctx.stat()) as StatAST
        return if (ctxStat is StatMultiAST) {
            WhileAST (ctx,
                visit(ctx.expr()) as ExprAST,
                ctxStat.stats
            )
        } else {
            WhileAST (ctx,
                visit(ctx.expr()) as ExprAST,
                mutableListOf(ctxStat)
            )
        }
    }

    override fun visitStatRead(ctx: WACCParser.StatReadContext): ASTNode {
        return super.visitStatRead(ctx)
    }

    override fun visitStatBegin(ctx: WACCParser.StatBeginContext): ASTNode {
        return super.visitStatBegin(ctx)
    }

    override fun visitStatDeclare(ctx: WACCParser.StatDeclareContext): ASTNode {
        return super.visitStatDeclare(ctx)
    }

    override fun visitStatAssign(ctx: WACCParser.StatAssignContext): ASTNode {
        return super.visitStatAssign(ctx)
    }

    override fun visitStatIf(ctx: WACCParser.StatIfContext): ASTNode {
        return super.visitStatIf(ctx)
    }

    override fun visitStatMulti(ctx: WACCParser.StatMultiContext): ASTNode {
        return super.visitStatMulti(ctx)
    }

    override fun visitStatSingle(ctx: WACCParser.StatSingleContext): ASTNode {
        return super.visitStatSingle(ctx)
    }

    override fun visitStatSkip(ctx: WACCParser.StatSkipContext): ASTNode {
        return super.visitStatSkip(ctx)
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

    override fun visitExprBinOp(ctx: WACCParser.ExprBinOpContext): ASTNode {
        return super.visitExprBinOp(ctx)
    }

    override fun visitExprBrackets(ctx: WACCParser.ExprBracketsContext): ASTNode {
        return super.visitExprBrackets(ctx)
    }

    override fun visitExprSingle(ctx: WACCParser.ExprSingleContext): ASTNode {
        return super.visitExprSingle(ctx)
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
}