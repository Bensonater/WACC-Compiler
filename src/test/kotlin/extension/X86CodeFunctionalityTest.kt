package extension

import backend.MapOfFilesToOutput
import getEachFile
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue

class X86CodeFunctionalityTest {
    private val map = MapOfFilesToOutput.getMap()

    /**
     * Tests the output of all valid WACC example files, comparing them to the output
     * of the reference compiler (testing functional correctness)
     */
    @Disabled
    @ParameterizedTest
    @MethodSource("testFiles")
    fun assemblyIsFunctionallyCorrect(file: File) {
        val name = file.nameWithoutExtension
        val refOutput = map[name]!!.first
        val refExit = map[name]!!.second

        ProcessBuilder("./compile", file.invariantSeparatorsPath, "-x86").start()
            .waitFor(20, TimeUnit.SECONDS)
        ProcessBuilder(
            "gcc", "-g", "-o", name, "$name.s", "-no-pie"
        ).start().waitFor(20, TimeUnit.SECONDS)

        var output: String
        val inputFile = File("reference_output/inputFiles/$name/input.txt")
        val process = if (inputFile.exists()) {
            ProcessBuilder("./$name").redirectInput(
                inputFile
            ).start()
        } else {
            ProcessBuilder("./$name").start()
        }

        process.waitFor(20, TimeUnit.SECONDS)

        process.inputStream.reader(Charsets.UTF_8).use {
            output = it.readText()
        }

        val success = ((refOutput == output) && (refExit == process.exitValue()))

        if (success) {
            println("- PASSED $name")
        } else {
            println("- FAILING $name -")
            println("------REFERENCE OUTPUT------")
            println(refOutput)
            println("------OUR OUTPUT------")
            println(output)
            println("----------------------")
            println("REFERENCE EXIT CODE: $refExit")
            println("OUR EXIT CODE: ${process.exitValue()}")
            println("----------------------")
        }
        ProcessBuilder("rm", "$name.s", name).start().waitFor(20, TimeUnit.SECONDS)
        assertTrue(success)
    }


    companion object {
        @JvmStatic
        fun testFiles(): List<File> {
            val root = "wacc_examples/valid"
            return getEachFile(File(root),
                listOf(
                    File("$root/array/array.wacc"),
                    File("$root/array/arrayPrint.wacc"),
                    File("$root/array/printRef.wacc"),
                    File("$root/function/simple_functions/functionManyArguments.wacc"),
                    File("$root/pairs/checkRefPair.wacc"),
                    File("$root/pairs/printPair.wacc"),
                    File("$root/pairs/printPairOfNulls.wacc"),
                    File("$root/scope/printAllTypes.wacc")
            ))
        }
    }
}