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
        assertEquals("(program begin (stat skip) end <EOF>)", tree.toStringTree(parser))
    }

    @Test
    fun parserReturnsPrintTree() {
        val input = CharStreams.fromString("begin print \"lorem ipsum\" end")
        val lexer = WACCLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = WACCParser(tokens)
        val tree = parser.program() // begin parsing at program rule
        assertEquals(
            "(program begin (stat print (expr (strLiter \"lorem ipsum\"))) end <EOF>)",
            tree.toStringTree(parser)
        )
    }

    @Test
    fun parserReturnsAssignmentTree() {
        val input = CharStreams.fromString("begin int i = 10 end")
        val lexer = WACCLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = WACCParser(tokens)
        val tree = parser.program() // begin parsing at program rule
        assertEquals(
            "(program begin (stat (type (baseType int)) (ident i) = " +
                    "(assignRhs (expr (intLiter 10)))) end <EOF>)", tree.toStringTree(parser)
        )
    }

    @Test
    fun parserReturnsSequenceTree() {
        val input = CharStreams.fromString(
            "begin\n" +
                    "    int i = 10;\n" +
                    "    print \"lorem ipsum\"\n" +
                    "end"
        )
        val lexer = WACCLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = WACCParser(tokens)
        val tree = parser.program() // begin parsing at program rule
        assertEquals(
            "(program begin (stat (stat (type (baseType int)) (ident i) = " +
                    "(assignRhs (expr (intLiter 10)))) ; (stat print (expr (strLiter \"lorem ipsum\")))) end <EOF>)",
            tree.toStringTree(parser)
        )
    }

    @Test
    fun parserReturnsCorrectOrderOfOperationsTree() {
        val input = CharStreams.fromString(
            "begin\n" +
                    "    int i = 10 + 5 * 6 - 2\n" +
                    "end"
        )
        val lexer = WACCLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = WACCParser(tokens)
        val tree = parser.program() // begin parsing at program rule
        assertEquals(
            "(program begin (stat (type (baseType int)) (ident i) = (assignRhs" +
                    " (expr (expr (expr (intLiter 10)) (binaryOper2 +) (expr " +
                    "(expr (intLiter 5)) (binaryOper1 *) (expr (intLiter 6)))) (binaryOper2 -) " +
                    "(expr (intLiter 2))))) end <EOF>)", tree.toStringTree(parser)
        )
    }
}