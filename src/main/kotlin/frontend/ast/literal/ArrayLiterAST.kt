package frontend.ast.literal

import frontend.SymbolTable
import frontend.ast.ExprAST
import frontend.ast.type.ArrayTypeAST
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

class ArrayLiterAST(val ctx: ParserRuleContext, val vals: List<ExprAST>) : TypeAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        for (value in vals) {
            if (!value.check(symbolTable)) {
                return false
            }
        }
        return true
    }

    lateinit var arrayType: TypeAST

    override fun getType(symbolTable: SymbolTable): TypeAST? {
        arrayType = if (vals.isEmpty()) {
            ArrayTypeAST(ctx, EmptyArrayAST(ctx), 1)
        } else {
            val exprType = vals[0].getType(symbolTable)
            if (exprType is ArrayTypeAST) {
                ArrayTypeAST(ctx, exprType.type, exprType.dimension + 1)
            } else {
                ArrayTypeAST(ctx, exprType!!, 1)
            }
        }
        for (elem in vals) {
            if (elem.getType(symbolTable) != arrayType) {
                // Call semantic error "Type within array is inconsistent"
                return null
            }
        }
        return arrayType
    }
}

class EmptyArrayAST(val ctx: ParserRuleContext) : TypeAST(ctx) {

}
