package backend

import TestUtils
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class GeneralCodeFunctionalityTest : TestUtils {
//    private val inputList = listOf(
//        "IOSequence",
//        "IOLoop",
//        "echoChar",
//        "echoInt",
//        "echoNegInt",
//        "echoBigInt",
//        "echoBigNegInt",
//        "echoPuncChar",
//        "read",
//        "fibonacciFullIt",
//        "fibonacciFullRec",
//        "rmStyleAddIO",
//        "readPair",
//        "printInputTriangle"
//    )

    @ParameterizedTest
    @ValueSource(
        strings = ["wacc_examples/valid/array", "wacc_examples/valid/basic",
            "wacc_examples/valid/expressions", "wacc_examples/valid/function",
            "wacc_examples/valid/if", "wacc_examples/valid/print", "wacc_examples/valid/pairs",
            "wacc_examples/valid/runtimeErr", "wacc_examples/valid/scope", "wacc_examples/valid/sequence",
            "wacc_examples/valid/variables", "wacc_examples/valid/while"]
    )
    fun assemblyIsFunctionallyCorrect(root: String) {
        val map = mapOutputsAndErrorCodes()
        var testsPassed = 0
        var testsRan = 0

        doForEachFile(File(root)) { file ->
            val name = file.nameWithoutExtension
            val path = file.invariantSeparatorsPath
            var process = ProcessBuilder("./compile", path).start()
            val refOutput = map[name]!!.first
            val refError = map[name]!!.second

            process.waitFor()
            process =
                ProcessBuilder(
                    "arm-linux-gnueabi-gcc",
                    "-o",
                    name,
                    "-mcpu=arm1176jzf-s",
                    "-mtune=arm1176jzf-s",
                    "$name.s"
                ).start()
            process.waitFor()
            process = ProcessBuilder("qemu-arm", "-L", "/usr/arm-linux-gnueabi", name).start()
            process.waitFor(5, TimeUnit.SECONDS)

            var output: String
            process.inputStream.reader(Charsets.UTF_8).use {
                output = it.readText()
            }

            if ((refOutput == output) && (refError == process.exitValue())) {
                println("PASSED $name")
                testsPassed++
            } else {
                println("FAILING $name")
                println("REFERENCE EC: $refError")
                println("OUR       EC: ${process.exitValue()}")
                println("------REFERENCE------")
                println(refOutput)
                println("------OUR------------")
                println(output)
            }

            process = ProcessBuilder("rm", "$name.s", name).start()
            process.waitFor()
            testsRan++
        }
        assertEquals(testsRan, testsPassed)
    }
}
