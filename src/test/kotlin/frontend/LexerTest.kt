package frontend

import antlr.WACCLexer
import org.antlr.v4.runtime.CharStreams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LexerTest {
    @Test
    fun lexerReturnsBasicProgramTokens() {
        val input = CharStreams.fromString("begin end")
        val lexer = WACCLexer(input)
        assertEquals("begin", lexer.nextToken().text)
        assertEquals("end", lexer.nextToken().text)
    }

    @Test
    fun lexerReturnsAssignmentTokens() {
        val input = CharStreams.fromString("begin int a = 10 end")
        val lexer = WACCLexer(input)
        assertEquals("begin", lexer.nextToken().text)
        assertEquals("int", lexer.nextToken().text)
        assertEquals("a", lexer.nextToken().text)
        assertEquals("=", lexer.nextToken().text)
        assertEquals("10", lexer.nextToken().text)
        assertEquals("end", lexer.nextToken().text)
    }

    @Test
    fun lexerReturnsBooleanTokens() {
        val input = CharStreams.fromString("begin true && false || true end")
        val lexer = WACCLexer(input)
        assertEquals("begin", lexer.nextToken().text)
        assertEquals("true", lexer.nextToken().text)
        assertEquals("&&", lexer.nextToken().text)
        assertEquals("false", lexer.nextToken().text)
        assertEquals("||", lexer.nextToken().text)
        assertEquals("true", lexer.nextToken().text)
        assertEquals("end", lexer.nextToken().text)
    }

    @Test
    fun lexerReturnsStringTokens() {
        val input = CharStreams.fromString("begin \"lorem ipsum\" end")
        val lexer = WACCLexer(input)
        assertEquals("begin", lexer.nextToken().text)
        assertEquals("\"lorem ipsum\"", lexer.nextToken().text)
        assertEquals("end", lexer.nextToken().text)
    }

    @Test
    fun lexerReturnsConditionalTokens() {
        val input = CharStreams.fromString("begin if true then else fi end")
        val lexer = WACCLexer(input)
        assertEquals("begin", lexer.nextToken().text)
        assertEquals("if", lexer.nextToken().text)
        assertEquals("true", lexer.nextToken().text)
        assertEquals("then", lexer.nextToken().text)
        assertEquals("else", lexer.nextToken().text)
        assertEquals("fi", lexer.nextToken().text)
        assertEquals("end", lexer.nextToken().text)
    }

    @Test
    fun lexerIgnoresWhitespace() {
        val input = CharStreams.fromString("begin   int a           =            10        end")
        val lexer = WACCLexer(input)
        assertEquals("begin", lexer.nextToken().text)
        assertEquals("int", lexer.nextToken().text)
        assertEquals("a", lexer.nextToken().text)
        assertEquals("=", lexer.nextToken().text)
        assertEquals("10", lexer.nextToken().text)
        assertEquals("end", lexer.nextToken().text)
    }

    @Test
    fun lexerIgnoresComments() {
        val input = CharStreams.fromString("begin #COMMENT \n end")
        val lexer = WACCLexer(input)
        assertEquals("begin", lexer.nextToken().text)
        assertEquals("end", lexer.nextToken().text)
    }
}