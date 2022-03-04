package backend

import frontend.TestUtils
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class CodeFunctionalityTest : TestUtils {
//    private val invalid = listOf(
//        "IOSequence.wacc",
//        "IOLoop.wacc",
//        "echoChar.wacc",
//        "echoInt.wacc",
//        "echoNegInt.wacc",
//        "echoBigInt.wacc",
//        "echoBigNegInt.wacc",
//        "echoPuncChar.wacc",
//        "read.wacc",
//        "fibonacciFullIt.wacc",
//        "fibonacciFullRec.wacc",
//        "rmStyleAddIO.wacc",
//        "readPair.wacc",
//        "printInputTriangle.wacc"
//    )

    private val inputMap = mapOf(
        "IOSequence" to "37\n",
        "IOLoop" to "1\nY\n2\nY\n3\nY\n4\nY\n5\nY\n142\nN\n",
        "echoChar" to "K\n",
        "echoInt" to "101\n",
        "echoNegInt" to "-5\n",
        "echoBigInt" to "2147483647\n",
        "echoBigNegInt" to "-2147483648\n",
        "echoPuncChar" to "!\n",
        "read" to "1\n",
        "fibonacciFullIt" to "30\n",
        "fibonacciFullRec" to "30\n",
        "rmStyleAddIO" to "2\n42\n",
        "readPair" to "f\n16\n",
        "printInputTriangle" to "13\n"
    )


    private fun mapOutputsAndErrorCodes(): HashMap<String, Pair<String, Int>> {
        val root = "wacc_examples/valid/"
        val map = HashMap<String, Pair<String, Int>>()

        doForEachFile(File(root)) { file ->
            val fileName =
                file.invariantSeparatorsPath.split(".wacc")[0].split("wacc_examples/valid/").last()
            val outputFile = File("reference_output/$fileName/output.txt")
            val errorFile = File("reference_output/$fileName/error.txt")
            val output = outputFile.inputStream().bufferedReader().use { it.readText() }
            val error = errorFile.inputStream().bufferedReader().use { it.readText() }.toInt()
            map.put(file.nameWithoutExtension, Pair(output, error))

        }
        return map
    }

    @ParameterizedTest
    @ValueSource(
        strings = ["wacc_examples/valid/array", "wacc_examples/valid/basic",
            "wacc_examples/valid/expressions", "wacc_examples/valid/function",
            "wacc_examples/valid/if", "wacc_examples/valid/IO", "wacc_examples/valid/pairs",
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
            process = if (inputMap.containsKey(name)) {
                val fileName = path.split(".wacc")[0].split("wacc_examples/valid/").last()
                val inputFile = File("reference_output/$fileName/input.txt")
                val inputStream = inputFile.inputStream()
                val input = inputStream.bufferedReader().use { it.readText() }
                println(input)

                ProcessBuilder("qemu-arm", "-L", "/usr/arm-linux-gnueabi", name).redirectInput(
                    inputFile
                ).start()
            } else {
                ProcessBuilder("qemu-arm", "-L", "/usr/arm-linux-gnueabi", name).start()
            }

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

//    @Test
//    fun newTest() {
//        val map = mapOutputsAndErrorCodes()
//        val root = "wacc_examples/valid/"
//        doForEachFile(File(root)) { file ->
//            val name = file.nameWithoutExtension
//            val refOutput = map[name]!!.first
//            val refError = map[name]!!.second
//            var output = ""
//            var error = "0"
//            var part =
//                file.inputStream().bufferedReader().use { it.readText() }.split("\n# Program:")
//                    .first().split("# Output:")
//                    .last()
//            if (part.contains("#empty#")) {
//                if (part.contains("# Exit:")) {
//                    error = part.split("# Exit:")[1].replace(" ", "").split("#").last().replace("\n","")
//                }
//            } else {
//                if (part.contains("# Exit:")) {
//                    error = part.split("# Exit:")[1].replace(" ", "").split("#").last().split("\n")
//                        .first()
//                }
//                output = part.replace("# ", "")
//            }
////            if (refOutput != output){
////                println("REF " + refOutput)
////                println("OUR " + output)
////            }
//            if(refError != error.toInt()){
//                println(name)
//                println(error)
//                println(refError)
//            }
//
//
////            if (part.substring(0,6) != "#empty#"){
////                output = part.split("# Exit:\n# ").first()
////            } else {
////                error = part.split("# Exit:\n# ")[1].split("\n").first().toInt()
////            }
////            println(output)
////            println(error)
//
//        }

}
