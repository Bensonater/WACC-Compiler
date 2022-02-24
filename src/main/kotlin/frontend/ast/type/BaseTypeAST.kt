package frontend.ast.type

import org.antlr.v4.runtime.ParserRuleContext

enum class BaseType {
    INT,
    BOOL,
    CHAR,
    STRING
}

/**
 * This AST node represents all the possible BaseTypes (Int, bool, char and string).
 * It allows for the type to be passed into the AST node.
 */
class BaseTypeAST (ctx: ParserRuleContext, val type:BaseType) : TypeAST(ctx) {

    override val size = when (type) {
        BaseType.INT, BaseType.STRING -> 4
        BaseType.BOOL, BaseType.CHAR -> 1
    }

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

    override fun toString(): String {
        return type.toString()
    }
}