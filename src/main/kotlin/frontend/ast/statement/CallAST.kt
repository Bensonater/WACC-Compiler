package frontend.ast.statement

import frontend.SymbolTable
import frontend.ast.ExprAST
import frontend.ast.FuncAST
import frontend.ast.IdentAST
import org.antlr.v4.runtime.ParserRuleContext

class CallAST(val ctx: ParserRuleContext, val ident: IdentAST, val args: List<ExprAST>) : StatAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        if (!ident.check(symbolTable)) {
            return false
        }
        val function = symbolTable.lookupAll(ident.name)
        if (function == null || function !is FuncAST) {
            // Call semantic error "Cannot find function $ident.name"
            return false
        }
        if (function.paramList.size != args.size) {
            // Call semantic error "Invalid number of arguments, expecting $function.paramList.size arguments"
            return false
        }
        for (i in args.indices) {
            if (!args[i].check(symbolTable)) {
                return false
            }
            val argType = args[i].getType(symbolTable)
            if (argType !== function.paramList[i].type) {
                // Call semantic error "The $ith argument has invalid type, expecting $function.paramList[i].type"
                return false
            }
        }
        return true
    }
}