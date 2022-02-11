package frontend

import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Test
import java.io.File
import antlr.*
import frontend.errors.SyntaxErrorHandler
import frontend.errors.SyntaxErrorListener
import frontend.visitor.BuildAST
import frontend.visitor.SyntaxChecker
import org.antlr.v4.runtime.CharStreams
import kotlin.test.assertTrue

class InvalidSemanticTest : TestUtils {
    @Test
    fun invalidFilesReturnSemanticError() {
        var newErrorCount = 0
        var totalTests = 0
        var failingTests = 0
        doForEachFile(File("wacc_examples/invalid/semanticErr")){ file ->
            var failedTest = false
            totalTests++

            val errorListener = SyntaxErrorListener()
            val input = CharStreams.fromStream(file.inputStream())
            val lexer = WACCLexer(input)
            lexer.removeErrorListeners()
            lexer.addErrorListener(errorListener)
            val tokens = CommonTokenStream(lexer)

            val parser = WACCParser(tokens)
            parser.removeErrorListeners()
            parser.addErrorListener(errorListener)
            val tree = parser.program()

            val syntaxErrorHandler = SyntaxErrorHandler()

            val checkSyntaxVisitor = SyntaxChecker(syntaxErrorHandler)
            checkSyntaxVisitor.visit(tree)

            if(syntaxErrorHandler.hasErrors() || parser.numberOfSyntaxErrors > 0){
                println("- SYNTAX ERROR - ")
                failedTest = true
            } else {
                val buildASTVisitor = BuildAST()

                val ast = buildASTVisitor.visit(tree)

                ast.check(SymbolTable())

                val oldErrorCount = newErrorCount
                newErrorCount = semanticErrorHandler.errorCount()

                if(newErrorCount - oldErrorCount == 0){
                    println("- NO SEMANTIC ERROR - ")
                    failedTest = true
                }
            }
            if (failedTest) {
                println("TEST " + file.name + " FAILED")
                failingTests++
            } else {
                println("TEST " + file.name + " PASSED")
            }
        }
        println("PASSING " + (totalTests - failingTests) + "/" + totalTests)
        assertTrue(failingTests == 0)
    }
}