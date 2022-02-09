package frontend.ast.type

import frontend.ast.ASTNode
import org.antlr.v4.runtime.ParserRuleContext

enum class BaseType {
    INT,
    BOOL,
    CHAR,
    STRING
}

class BaseTypeAST (ctx: ParserRuleContext, val type:BaseType) : TypeAST(ctx) {
}