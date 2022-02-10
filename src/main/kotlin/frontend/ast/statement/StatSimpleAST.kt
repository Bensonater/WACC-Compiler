package frontend.ast.statement

import frontend.SymbolTable
import frontend.ast.ExprAST
import frontend.ast.type.ArrayTypeAST
import frontend.ast.type.BaseType
import frontend.ast.type.BaseTypeAST
import frontend.ast.type.PairTypeAST
import org.antlr.v4.runtime.ParserRuleContext

enum class Command {
    FREE, RETURN, EXIT, PRINT, PRINTLN
}

class StatSimpleAST(ctx: ParserRuleContext, val command: Command, val expr: ExprAST) : StatAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        if (!expr.check(symbolTable)) {
            return false
        }
        val exprType = expr.getType(symbolTable)
        if (command == Command.EXIT && (exprType !is BaseTypeAST || exprType.type != BaseType.INT)) {
            // Call semantic error "Exit code should be of type Int"
            return false
        }
        if (command == Command.FREE && exprType !is PairTypeAST && exprType !is ArrayTypeAST) {
            // Call semantic error "Free can only take pair or array type"
            return false
        }
        if (command == Command.RETURN) {
            val parentFuncType = symbolTable.funcTypeLookUp()
            if (parentFuncType == null) {
                // Call semantic error "Return statement is not in a function"
                return false
            }
            if (exprType != parentFuncType) {
                // Call semantic error "Return statement type mismatch"
                return false
            }
        }
        return true
    }
}
