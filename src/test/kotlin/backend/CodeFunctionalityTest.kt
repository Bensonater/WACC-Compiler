package backend

import frontend.TestUtils
import org.junit.Ignore
import org.junit.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class CodeFunctionalityTest : TestUtils {
    val invalid = listOf<String>(
        "IOSequence.wacc",
        "IOLoop.wacc",
        "echoChar.wacc",
        "echoInt.wacc",
        "echoNegInt.wacc",
        "echoBigInt.wacc",
        "echoBigNegInt.wacc",
        "echoPuncChar.wacc",
        "read.wacc",
        "fibonacciFullIt.wacc",
        "fibonacciFullRec.wacc",
        "rmStyleAddIO.wacc",
        "readPair.wacc",
        "printInputTriangle.wacc"
    )


    private fun mapOutputsAndErrorCodes(): HashMap<String, Pair<String, Int>> {
        val root = "wacc_examples/valid/"
        val map = HashMap<String, Pair<String, Int>>()

        doForEachFile(File(root)) { file ->
            val fileName = file.invariantSeparatorsPath.split(".wacc")[0].split("wacc_examples/valid/").last()
            val outputFile = File("reference_output/$fileName/output.txt")
            val errorFile = File("reference_output/$fileName/error.txt")
            val output = outputFile.inputStream().bufferedReader().use { it.readText() }
            val error = errorFile.inputStream().bufferedReader().use { it.readText() }.toInt()
            map.put(file.nameWithoutExtension, Pair(output, error))

        }
        return map
    }

    @ParameterizedTest
    @ValueSource(strings = [ "wacc_examples/valid/"])
    fun assemblyIsFunctionallyCorrect(root:String) {
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
            if (invalid.contains(file.name)) {
                val fileName = path.split(".wacc")[0].split("wacc_examples/valid/").last()
                val inputFile = File("reference_output/$fileName/input.txt")
                val inputStream = inputFile.inputStream()
                val input = inputStream.bufferedReader().use { it.readText() }
                println(input)

                process =
                    ProcessBuilder("qemu-arm", "-L", "/usr/arm-linux-gnueabi", name).redirectInput(inputFile).start()
            } else {
                process = ProcessBuilder("qemu-arm", "-L", "/usr/arm-linux-gnueabi", name).start()
            }

            process.waitFor(5, TimeUnit.SECONDS)

            var output: String
            process.inputStream.reader(Charsets.UTF_8).use {
                output = it.readText()
            }

            if ((refOutput.equals(output)) && (refError == process.exitValue())) {
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