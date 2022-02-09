package frontend.ast.literal

import frontend.ast.ASTNode
import frontend.ast.ExprAST
import frontend.SymbolTable
import org.antlr.v4.runtime.ParserRuleContext

class ArrayLiterAST (ctx: ParserRuleContext, val vals: List<ExprAST>) : ASTNode(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        for (value in vals) {
            if (!value.check(symbolTable)){
                return false
            }
        }
        return true
    }
}