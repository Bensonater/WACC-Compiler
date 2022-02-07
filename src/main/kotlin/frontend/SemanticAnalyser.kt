package frontend

import antlr.*
import org.antlr.v4.runtime.*

fun main() {
    val input = CharStreams.fromStream(System.`in`)
    val lexer = WACCLexer(input)
    val tokens = CommonTokenStream(lexer)
    val parser = WACCParser(tokens)
    val tree = parser.program()
    println(tree.toStringTree(parser))
}