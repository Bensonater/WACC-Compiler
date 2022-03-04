package backend

import TestUtils
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class InputFileCodeFunctionalityTest : TestUtils {
    @Test
    fun assemblyIsFunctionallyCorrect() {
        val root = "wacc_examples/valid/inputFiles"
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
            val inputFile = File("reference_output/$name/input.txt")
            process =
                ProcessBuilder("qemu-arm", "-L", "/usr/arm-linux-gnueabi", name).redirectInput(
                    inputFile
                ).start()
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