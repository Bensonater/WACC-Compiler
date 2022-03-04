package backend

import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class InputFileCodeFunctionalityTest : TestUtils {
    @Test
    fun assemblyIsFunctionallyCorrect() {
        val map = mapOutputsAndErrorCodes()
        var testsPassed = 0
        var testsRan = 0

        doForEachFile(File("wacc_examples/valid/inputFiles")) { file ->
            val name = file.nameWithoutExtension
            val refOutput = map[name]!!.first
            val refError = map[name]!!.second

            ProcessBuilder("./compile", file.invariantSeparatorsPath).start()
                .waitFor(5, TimeUnit.SECONDS)
            ProcessBuilder(
                "arm-linux-gnueabi-gcc",
                "-o",
                name,
                "-mcpu=arm1176jzf-s",
                "-mtune=arm1176jzf-s",
                "$name.s"
            ).start().waitFor(5, TimeUnit.SECONDS)

            var output: String
            val inputFile = File("reference_output/$name/input.txt")
            val process =
                ProcessBuilder("qemu-arm", "-L", "/usr/arm-linux-gnueabi", name).redirectInput(
                    inputFile
                ).start()
            process.waitFor(5, TimeUnit.SECONDS)

            process.inputStream.reader(Charsets.UTF_8).use {
                output = it.readText()
            }

            if ((refOutput == output) && (refError == process.exitValue())) {
                println("- PASSED $name")
                testsPassed++
            } else {
                println("- FAILING $name -")
                println("------REFERENCE OUTPUT------")
                println(refOutput)
                println("------OUR OUTPUT------")
                println(output)
                println("REFERENCE ERROR CODE: $refError")
                println("OUR ERROR CODE: ${process.exitValue()}")
            }

            ProcessBuilder("rm", "$name.s", name).start().waitFor(5, TimeUnit.SECONDS)
            testsRan++
        }
        assertEquals(testsRan, testsPassed)
    }
}