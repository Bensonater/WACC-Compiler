package frontend.ast.statement

import frontend.SymbolTable
import frontend.ast.ExprAST
import frontend.ast.type.BaseType
import frontend.ast.type.BaseTypeAST
import frontend.ast.type.PairTypeAST
import org.antlr.v4.runtime.ParserRuleContext

enum class Command {
    FREE, RETURN, EXIT, PRINT, PRINTLN
}

class StatSimpleAST(ctx: ParserRuleContext, val command: Command, val expr: ExprAST) : StatAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        if (!expr.check(symbolTable)) {
            return false
        }
        val exprType = expr.getType(symbolTable)
        if (command == Command.EXIT) {
            return exprType is BaseTypeAST && exprType.type == BaseType.INT
        }
        if (command == Command.FREE) {
            return exprType is PairTypeAST
        }

        return true
    }
}
