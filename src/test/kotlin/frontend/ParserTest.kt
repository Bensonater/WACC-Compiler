package frontend

import antlr.WACCLexer
import antlr.WACCParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class ParserTest {

    @Test
    fun parserReturnsBasicProgramTreeWithSkip() {
        val input = CharStreams.fromString("begin skip end")
        val lexer = WACCLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = WACCParser(tokens)
        val tree = parser.program() // begin parsing at program rule
        assertEquals("(program begin (stat skip) end)", tree.toStringTree(parser))
    }

    @Test
    fun parserReturnsPrintTree() {
        val input = CharStreams.fromString("begin print \"lorem ipsum\" end")
        val lexer = WACCLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = WACCParser(tokens)
        val tree = parser.program() // begin parsing at program rule
        assertEquals("(program begin (stat print (expr \"lorem ipsum\")) end)", tree.toStringTree(parser))
    }

    @Test
    fun parserReturnsAssignmentTree() {
        val input = CharStreams.fromString("begin int i = 10 end")
        val lexer = WACCLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = WACCParser(tokens)
        val tree = parser.program() // begin parsing at program rule
        assertEquals("(program begin (stat (type (baseType int)) i = (assignRhs (expr 10))) end)", tree.toStringTree(parser))
    }

    @Test
    fun parserReturnsSequenceTree() {
        val input = CharStreams.fromString("begin\n" +
                "    int i = 10;\n" +
                "    print \"lorem ipsum\"\n" +
                "end")
        val lexer = WACCLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = WACCParser(tokens)
        val tree = parser.program() // begin parsing at program rule
        assertEquals("(program begin (stat (stat (type (baseType int)) " +
                "i = (assignRhs (expr 10))) ; (stat print (expr \"lorem ipsum\"))) end)", tree.toStringTree(parser))
    }

    @Test
    fun parserReturnsCorrectOrderOfOperationsTree() {
        val input = CharStreams.fromString("begin\n" +
                "    int i = 10 + 5 * 6 - 2\n" +
                "end")
        val lexer = WACCLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = WACCParser(tokens)
        val tree = parser.program() // begin parsing at program rule
        assertEquals("(program begin (stat (type (baseType int)) i = (assignRhs (expr " +
                "(expr (expr 10) (binaryOper2 +) (expr (expr 5) (binaryOper1 *) (expr 6))) " +
                "(binaryOper2 -) (expr 2)))) end)", tree.toStringTree(parser))
    }
}