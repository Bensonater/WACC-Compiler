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
    val root = "wacc_examples/invalid/syntaxErr"

    /**
     * Tests all the invalid WACC example files with syntax errors, ensuring that at least
     * one syntax error is returned
     */
    @Test
    fun invalidFilesReturnSyntaxError() {
        var totalTests = 0
        var failingTests = 0
        doForEachFile(File(root)) { file ->
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

            if (!(syntaxErrorHandler.hasErrors() || parser.numberOfSyntaxErrors > 0)) {
                failingTests++
            }
        }
        println("PASSING " + (totalTests - failingTests) + "/" + totalTests)
        assertTrue(failingTests == 0)
    }


}