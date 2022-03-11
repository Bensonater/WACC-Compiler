import backend.Language
import org.antlr.v4.runtime.CharStreams
import kotlin.system.exitProcess
import frontend.errors.*
import java.io.File

val language = Language.ARM

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Error - No WACC file specified.")
        exitProcess(OTHER_ERROR_CODE)
    }
    if (!File(args[0]).exists()) {
        println ("Error - ${args[0]} does not exist.")
        exitProcess(OTHER_ERROR_CODE)
    }
    val input = CharStreams.fromFileName(args[0])

    val astStatusPair = frontend.main(input)
    if (astStatusPair.first != SUCCESS_CODE) {
        exitProcess(astStatusPair.first)
    }

    val ast = astStatusPair.second!!

    val code = backend.main(ast)

    // Creates an assembly file and write the instructions
    val fileName = args[0].split(".wacc")[0].split("/").last()
    val file = File("$fileName.s")
    file.writeText(code)

    exitProcess(SUCCESS_CODE)
}