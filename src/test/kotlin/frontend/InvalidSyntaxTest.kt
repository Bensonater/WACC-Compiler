package frontend

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Test
import java.io.File
import antlr.*
import frontend.errors.SyntaxErrorHandler
import frontend.errors.SyntaxErrorListener
import frontend.visitor.SyntaxChecker
import kotlin.test.assertTrue

class InvalidSyntaxTest : TestUtils {
    @Test
    fun invalidFilesReturnSyntaxError() {
        doForEachFile(File("wacc_examples/invalid/syntaxErr")){ file ->
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

            assertTrue(syntaxErrorHandler.hasErrors() || parser.numberOfSyntaxErrors > 0)
            }
        }



}