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
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseTypeAST

        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }
}