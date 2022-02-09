package frontend.ast.literal

import frontend.SymbolTable
import frontend.ast.ExprAST
import frontend.ast.type.BaseType
import frontend.ast.type.BaseTypeAST
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

class BoolLiterAST  (val ctx: ParserRuleContext, val value: Boolean) : ExprAST(ctx) {
    override fun getType(symbolTable: SymbolTable): TypeAST? {
        return BaseTypeAST(ctx, BaseType.BOOL)
    }
}