package frontend

import antlr.*
import frontend.errors.SUCCESS_CODE
import frontend.errors.SYNTAX_ERROR_CODE
import frontend.errors.SyntaxErrorHandler
import frontend.errors.SyntaxErrorListener
import frontend.visitor.SyntaxChecker
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

        val syntaxErrorHandler = SyntaxErrorHandler()

        val checkSyntaxVisitor = SyntaxChecker(syntaxErrorHandler)
        checkSyntaxVisitor.visit(tree)


        if (syntaxErrorHandler.hasErrors()) {
            syntaxErrorHandler.printErrors()
            return SYNTAX_ERROR_CODE
        }
        // 1. syntaxCheck
        // 2. buildAst (Traverse parseTree)
        // 3. semanticCheck (Visitor)

//    val visitor = AstVisitor()
//    ASTNode ast = visitor.visit(tree)

        return SUCCESS_CODE
    }
}