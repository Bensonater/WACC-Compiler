import backend.BackendMain
import org.antlr.v4.runtime.CharStreams
import frontend.FrontendMain
import kotlin.system.exitProcess
import frontend.errors.*
import java.io.File

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

    val astStatusPair = FrontendMain.main(input)
    if (astStatusPair.first != SUCCESS_CODE) {
        exitProcess(astStatusPair.first)
    }

    val ast = astStatusPair.second


    exitProcess(SUCCESS_CODE)
}