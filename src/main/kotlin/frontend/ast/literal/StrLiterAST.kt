package frontend.ast.literal

import frontend.SymbolTable
import frontend.ast.ExprAST
import frontend.ast.type.BaseType
import frontend.ast.type.BaseTypeAST
import frontend.ast.type.TypeAST
import org.antlr.v4.runtime.ParserRuleContext

/**
 * AST node representing a string literal without the quotation marks.
 * E.g. "Hello world!" has the value Hello world!
 */
class StrLiterAST(val ctx: ParserRuleContext, val value: String) : ExprAST(ctx) {
    override fun getType(symbolTable: SymbolTable): TypeAST {
        return BaseTypeAST(ctx, BaseType.STRING)
    }
}