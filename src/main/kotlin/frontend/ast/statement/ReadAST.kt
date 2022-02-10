package frontend.ast.statement

import frontend.SymbolTable
import frontend.ast.ASTNode
import frontend.ast.type.BaseType
import frontend.ast.type.BaseTypeAST
import frontend.semanticErrorHandler
import org.antlr.v4.runtime.ParserRuleContext

class ReadAST(val ctx: ParserRuleContext, val assignLhs: ASTNode) : StatAST(ctx) {
    override fun check(symbolTable: SymbolTable): Boolean {
        this.symbolTable = symbolTable
        if (!assignLhs.check(symbolTable)) {
            return false
        }
        if (assignLhs.getType(symbolTable) != BaseTypeAST(ctx, BaseType.CHAR) &&
                assignLhs.getType(symbolTable) != BaseTypeAST(ctx, BaseType.INT)) {
            semanticErrorHandler.invalidReadType(ctx)
            return false
        }
        return true
    }
}
