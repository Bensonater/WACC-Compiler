package backend

import frontend.errors.SUCCESS_CODE
import org.antlr.v4.runtime.CharStreams
import org.junit.jupiter.api.Test
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class BasicGenerationTest {
    val root = "wacc_examples/valid/basic/exit/exitBasic.wacc"

    @Test
    fun assemblyIsFunctionallyCorrect() {
        val input = CharStreams.fromFileName(root)

        val astStatusPair = frontend.main(input)
        if (astStatusPair.first != SUCCESS_CODE) {
            exitProcess(astStatusPair.first)
        }

        val ast = astStatusPair.second!!

        val code = main(ast)

//        // Creates an assembly file and write the instructions
//        val fileName = root.split(".wacc")[0].split("/").last()
//        val file = File("$fileName.s")
//        file.writeText(code)
//
//        Runtime.getRuntime().exec("arm-linux-gnueabi-gcc -o exitBasic -mcpu=arm1176jzf-s -mtune=arm1176jzf-s exitBasic.s")
//        val process2 = Runtime.getRuntime().exec("qemu-arm -L /usr/arm-linux-gnueabi exitBasic")
//        process2.inputStream.reader(Charsets.UTF_8).use {
//            println(it.readText())
//        }
//        process2.waitFor(5, TimeUnit.SECONDS)
//        println(process2.exitValue())
    }

}