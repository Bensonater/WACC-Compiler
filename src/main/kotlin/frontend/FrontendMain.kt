package frontend

import antlr.*
import frontend.errors.SUCCESS_CODE
import frontend.errors.SYNTAX_ERROR_CODE
import frontend.errors.SyntaxErrorListener
import org.antlr.v4.runtime.*

object FrontendMain {
    fun main(input: CharStream) : Int {

        val errorListener = SyntaxErrorListener()

        val lexer = WACCLexer(input)
        lexer.removeErrorListeners()
        lexer.addErrorListener(errorListener)
        val tokens = CommonTokenStream(lexer)

        val parser = WACCParser(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(errorListener)
        val tree = parser.program()

        if (parser.numberOfSyntaxErrors > 0) {
            println("There were ${parser.numberOfSyntaxErrors} syntax errors in the program.")
            return SYNTAX_ERROR_CODE
        }


        println(tree.toStringTree(parser))
//    val visitor = AstVisitor()
//    ASTNode ast = visitor.visit(tree)

        return SUCCESS_CODE
    }
}