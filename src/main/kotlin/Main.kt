import org.antlr.v4.runtime.CharStreams
import frontend.FrontendMain

fun main() {
    val input = CharStreams.fromStream(System.`in`)
    FrontendMain.main(input)
}