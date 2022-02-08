package frontend

import antlr.*
import org.antlr.v4.runtime.*

object FrontendMain {
    fun main(input: CharStream) {

        val lexer = WACCLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = WACCParser(tokens)
        val tree = parser.program()
        println(tree.toStringTree(parser))
//    val visitor = AstVisitor()
//    ASTNode ast = visitor.visit(tree)


    }
}