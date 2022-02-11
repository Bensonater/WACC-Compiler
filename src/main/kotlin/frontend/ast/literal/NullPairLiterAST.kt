package frontend.ast.literal

import frontend.SymbolTable
import frontend.ast.ExprAST
import frontend.ast.type.ArbitraryTypeAST
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

class NullPairLiterAST (val ctx: ParserRuleContext) : ExprAST(ctx) {

    override fun getType(symbolTable: SymbolTable): TypeAST {
        return ArbitraryTypeAST(ctx)
    }
}