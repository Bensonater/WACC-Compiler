package frontend.ast

import frontend.SymbolTable
import frontend.ast.statement.StatAST
import frontend.ast.type.BaseType
import frontend.ast.type.BaseTypeAST
import org.antlr.v4.runtime.ParserRuleContext

class ProgramAST(val ctx: ParserRuleContext, val funcList: List<FuncAST>, val stat: StatAST) : ASTNode(ctx) {
    /* Inserts all base type into Symbol Table */
    init {
        this.symbolTable = SymbolTable()
        symbolTable.put("int", BaseTypeAST(ctx, BaseType.INT))
        symbolTable.put("bool", BaseTypeAST(ctx, BaseType.BOOL))
        symbolTable.put("char", BaseTypeAST(ctx, BaseType.CHAR))
        symbolTable.put("string", BaseTypeAST(ctx, BaseType.STRING))
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        for (func in funcList) {
            if (symbolTable.get(func.ident.name) != null) {
                // Call semantic error "Function $func.ident.name is already defined."
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