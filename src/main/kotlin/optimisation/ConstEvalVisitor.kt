package optimisation

import frontend.ast.*
import frontend.ast.literal.BoolLiterAST
import frontend.ast.literal.CharLiterAST
import frontend.ast.literal.IntLiterAST
import frontend.ast.statement.*

class ConstEvalVisitor : OptimisationVisitor() {

    override fun visitAssignAST(ast: AssignAST): ASTNode {
        val assignAST = AssignAST(ast.ctx, ast.assignLhs, ast.assignRhs)
        assignAST.symbolTable = ast.symbolTable
        return assignAST
    }

    override fun visitStatSimpleAST(ast: StatSimpleAST): ASTNode {
        val statSimpleAST = StatSimpleAST(ast.ctx, ast.command, visit(ast.expr) as ExprAST)
        statSimpleAST.symbolTable = ast.symbolTable
        return statSimpleAST
    }

    override fun visitDeclareAST(ast: DeclareAST): ASTNode {
        val rhs = visit(ast.assignRhs)
        val declareAST = DeclareAST(ast.ctx, ast.type, ast.ident, rhs)
        ast.symbolTable.updateVariable(ast.ident.name, rhs)
        declareAST.symbolTable = ast.symbolTable
        return declareAST
    }

    override fun visitStatMultiAST(ast: StatMultiAST): ASTNode {
        val stats = mutableListOf<StatAST>()
        for (stat in ast.stats) {
            stats.add(visit(stat) as StatAST)
        }
        return StatMultiAST(ast.ctx, stats)
    }

    override fun visitReadAST(ast: ReadAST): ASTNode {
        val readAST = ReadAST(ast.ctx, visit(ast.assignLhs))
        readAST.symbolTable = ast.symbolTable
        return readAST
    }

    override fun visitNewPairAST(ast: NewPairAST): ASTNode {
        return NewPairAST(ast.ctx, visit(ast.fst) as ExprAST, visit(ast.snd) as ExprAST)
    }

    override fun visitCallAST(ast: CallAST): ASTNode {
        val args = mutableListOf<ExprAST>()
        for (arg in ast.args) {
            args.add(visit(arg) as ExprAST)
        }
        val callAST = CallAST(ast.ctx, ast.ident, args)
        callAST.symbolTable = ast.symbolTable
        return callAST
    }

    override fun visitUnOpExprAST(ast: UnOpExprAST): ASTNode {
        val expr = visit(ast.expr)
        return when {
            (expr is IntLiterAST) && (ast.unOp == UnOp.CHR) -> {
                CharLiterAST(ast.ctx, expr.value.toChar())
            }
            (expr is IntLiterAST) && (ast.unOp == UnOp.MINUS) -> {
                IntLiterAST(ast.ctx, -expr.value)
            }
            (expr is CharLiterAST) && (ast.unOp == UnOp.ORD) -> {
                IntLiterAST(ast.ctx, expr.value.code)
            }
            (expr is BoolLiterAST) && (ast.unOp == UnOp.NOT) -> {
                BoolLiterAST(ast.ctx, !expr.value)
            }
            else -> ast
        }
    }

    override fun visitBinOpExprAST(ast: BinOpExprAST): ASTNode {
        val expr1 = visit(ast.expr1)
        val expr2 = visit(ast.expr2)

        return when (ast.binOp) {
            is IntBinOp -> {
                val val1 = (expr1 as IntLiterAST).value
                val val2 = (expr2 as IntLiterAST).value
                if ((val2 == 0) && ((ast.binOp == IntBinOp.DIV) || (ast.binOp == IntBinOp.MOD))) {
                    return ast
                }
                return IntLiterAST(ast.ctx, apply(ast.binOp, val1, val2))
            }
            is BoolBinOp -> {
                return BoolLiterAST(
                    ast.ctx,
                    apply(ast.binOp, (expr1 as BoolLiterAST).value, (expr2 as BoolLiterAST).value)
                )
            }
            is CmpBinOp -> {
                if (expr1 is IntLiterAST && expr2 is IntLiterAST) {
                    val val1 = expr1.value
                    val val2 = expr2.value
                    when (ast.binOp) {
                        CmpBinOp.GTE -> BoolLiterAST(ast.ctx, val1 >= val2)
                        CmpBinOp.GT -> BoolLiterAST(ast.ctx, val1 > val2)
                        CmpBinOp.LTE -> BoolLiterAST(ast.ctx, val1 < val2)
                        CmpBinOp.LT -> BoolLiterAST(ast.ctx, val1 <= val2)
                        CmpBinOp.EQ -> BoolLiterAST(ast.ctx, val1 == val2)
                        CmpBinOp.NEQ -> BoolLiterAST(ast.ctx, val1 != val2)
                    }
                } else {
                    val val1 = (expr1 as BoolLiterAST).value
                    val val2 = (expr2 as BoolLiterAST).value
                    when (ast.binOp) {
                        CmpBinOp.EQ -> BoolLiterAST(ast.ctx, val1 == val2)
                        CmpBinOp.NEQ -> BoolLiterAST(ast.ctx, val1 != val2)
                        else -> ast
                    }
                }
            }
            else -> ast
        }
    }

    private fun apply(op: IntBinOp, val1: Int, val2: Int): Int {
        return when (op) {
            IntBinOp.PLUS -> {
                val1 + val2
            }
            IntBinOp.MINUS -> {
                val1 - val2
            }
            IntBinOp.MULT -> {
                val1 * val2
            }
            IntBinOp.DIV -> {
                val1 / val2
            }
            IntBinOp.MOD -> {
                val1 % val2
            }
        }
    }

    private fun apply(op: BoolBinOp, val1: Boolean, val2: Boolean): Boolean {
        return when (op) {
            BoolBinOp.AND -> {
                val1 && val2
            }
            BoolBinOp.OR -> {
                val1 || val2
            }
        }
    }
}
