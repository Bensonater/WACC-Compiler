package optimisation

import frontend.ast.ASTNode
import frontend.ast.ExprAST
import frontend.ast.IdentAST
import frontend.ast.statement.*

class InstrEvalVisitor : OptimisationVisitor() {
    override fun visitIfAST(ast: IfAST): ASTNode {
        val ifStat = super.visitIfAST(ast)
        if (ifStat is IfAST) {
            var thenBody = ifStat.thenStat
            var elseBody = ifStat.elseStat
            var changed = false
            if (thenBody.isEmpty() && elseBody.isEmpty()) {
                return SkipAST(ifStat.ctx)
            }
            if (thenBody.isEmpty()) {
                thenBody = listOf(SkipAST(ifStat.ctx))
                changed = true
            }
            if (elseBody.isEmpty()) {
                elseBody = listOf(SkipAST(ifStat.ctx))
                changed = true
            }
            if (changed) {
                val optimisedIfStat = IfAST(ifStat.ctx, ifStat.expr, thenBody, elseBody)
                optimisedIfStat.symbolTable = ifStat.symbolTable
                optimisedIfStat.thenSymbolTable = ifStat.thenSymbolTable
                optimisedIfStat.elseSymbolTable = ifStat.elseSymbolTable
                optimisedIfStat.thenReturns = ifStat.thenReturns
                optimisedIfStat.elseReturns = ifStat.elseReturns
                return optimisedIfStat
            }
        }
        return ifStat
    }

    override fun visitWhileAST(ast: WhileAST): ASTNode {
        val whileCond = visit(ast.expr) as ExprAST
        val bodyStats = mutableListOf<StatAST>()
        for (stat in ast.stats) {
            bodyStats.add(visit(stat) as StatAST)
        }
        if (bodyStats.isEmpty()) {
            return SkipAST(ast.ctx)
        }
        val whileAST = WhileAST(ast.ctx, whileCond, bodyStats)
        whileAST.symbolTable = ast.symbolTable
        whileAST.bodySymbolTable = ast.bodySymbolTable
        return whileAST
    }

    override fun visitDeclareAST(ast: DeclareAST): ASTNode {
        val identAst = visit(ast.ident)
        if (identAst is SkipAST) {
            return SkipAST(ast.ctx)
        }
        return ast
    }

    override fun visitIdentAST(ast: IdentAST): ASTNode {

        /*     val accessed = ast.symbolTable.(ast.name)
             if (!accessed) {
                 ast.symbolTable.removeOptimisedVariableFromST(ast.name)
                 return symbolTable()
             }
        */
        return ast
    }
}