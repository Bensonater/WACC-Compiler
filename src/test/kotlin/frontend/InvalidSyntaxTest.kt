package frontend

import antlr.WACCLexer
import antlr.WACCParser
import frontend.errors.SyntaxErrorHandler
import frontend.errors.SyntaxErrorListener
import frontend.visitor.SyntaxChecker
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

class InvalidSyntaxTest : TestUtils {
    @Test
    fun invalidFilesReturnSyntaxError() {
        var totalTests = 0
        var failingTests = 0
        doForEachFile(File("wacc_examples/invalid/syntaxErr")) { file ->
            println("- TESTING: " + file.name)
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

            if (syntaxErrorHandler.hasErrors() || parser.numberOfSyntaxErrors > 0) {
                println("   TEST " + file.name + " PASSED")
            } else {
                println("X NO SYNTAX ERROR X")
                println("   TEST " + file.name + " FAILED")
                failingTests++
            }
        }
        println("PASSING " + (totalTests - failingTests) + "/" + totalTests)
        assertTrue(failingTests == 0)
    }


}