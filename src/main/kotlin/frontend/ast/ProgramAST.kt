package frontend.ast

import frontend.SymbolTable
import frontend.ast.literal.BoolLiterAST
import frontend.ast.literal.CharLiterAST
import frontend.ast.literal.IntLiterAST
import frontend.ast.literal.StrLiterAST
import frontend.ast.statement.StatAST
import org.antlr.v4.runtime.ParserRuleContext

class ProgramAST(val ctx: ParserRuleContext, val funcList: List<FuncAST>, val stat: StatAST) : ASTNode(ctx) {
    override var symbolTable = SymbolTable()

    /* Inserts all base type into Symbol Table */
    init{
        symbolTable.put("int", IntLiterAST(ctx, 1))
        symbolTable.put("bool", BoolLiterAST(ctx, true))
        symbolTable.put("char", CharLiterAST(ctx, 'a'))
        symbolTable.put("string", StrLiterAST(ctx, ""))
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        for (func in funcList) {
            if (symbolTable.get(func.ident.name) != null) {
                // Return semantic error "Function is already defined {func.ident.name}"
                return false
            }
            symbolTable.put(func.ident.name, func)
        }
        for (func in funcList) {
            func.check(symbolTable)
        }
        stat.check(symbolTable)
        return true
    }

}