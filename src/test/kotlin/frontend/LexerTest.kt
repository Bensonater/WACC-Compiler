package frontend

import antlr.WACCLexer
import org.antlr.v4.runtime.CharStreams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class LexerTest {
    @Test
    fun lexerReturnsCorrectTokens() {
        val input = CharStreams.fromString("begin int a = 10 end")
        val lexer = WACCLexer(input)
        assertEquals(lexer.nextToken().text, "begin")
        assertEquals(lexer.nextToken().text, "int")
        lexer.nextToken()
        assertEquals(lexer.nextToken().text, "=")
        lexer.nextToken()
        assertEquals(lexer.nextToken().text, "end")
    }
}