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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InvalidSemanticTest {
    @Test
    fun invalidFilesReturnSemanticError() {
        doForEachFile(File("wacc_examples/invalid/semanticErr")){ file ->
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

            assertFalse(syntaxErrorHandler.hasErrors() || parser.numberOfSyntaxErrors > 0)

            val buildASTVisitor = BuildAST()

            val ast = buildASTVisitor.visit(tree)

            ast.check(SymbolTable())

            assertTrue(semanticErrorHandler.hasErrors())
        }
    }


    fun <T> doForEachFile(file: File, operation: (File) -> T): List<T>{
        val operatedList = emptyList<T>().toMutableList()
        if(file.isDirectory){
            for (subFile in file.listFiles()!!){
                operatedList += doForEachFile(subFile, operation)
            }
        } else {
            operatedList += operation(file)
        }
        return operatedList
    }
}