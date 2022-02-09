package frontend.ast

import frontend.SymbolTable
import frontend.ast.statement.StatAST
import org.antlr.v4.runtime.ParserRuleContext

class ProgramAST(ctx: ParserRuleContext, funcList: List<FuncAST>, statAST: StatAST) : ASTNode(ctx) {
    override var symbolTable = SymbolTable()

    override fun check(symbolTable: SymbolTable): Boolean {
        return true
    }

}