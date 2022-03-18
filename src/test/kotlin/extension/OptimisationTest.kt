package extension

import getEachFile
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue

class OptimisationTest {
    private val map = getAssemblyMap()

    /**
     * Tests the output of all valid WACC example files, comparing them to the output
     * of the reference compiler (testing functional correctness)
     */
    @ParameterizedTest
    @MethodSource("testFiles")
    fun assemblyIsOptimised(file: File) {
        val name = file.nameWithoutExtension
        val optimisedCode = map[name]

        ProcessBuilder("./compile", file.invariantSeparatorsPath, "-o").start()
            .waitFor(20, TimeUnit.SECONDS)
        ProcessBuilder(
            "arm-linux-gnueabi-gcc",
            "-o",
            name,
            "-mcpu=arm1176jzf-s",
            "-mtune=arm1176jzf-s",
            "$name.s"
        ).start().waitFor(20, TimeUnit.SECONDS)

        val assemblyCode = File("$name.s").inputStream().bufferedReader().use { it.readText() }

        val success = (assemblyCode == optimisedCode)

        if (success) {
            println("- PASS: Assembly Optimised for $name")
        } else {
            println("- FAIL: Assembly NOT Optimised for $name")
            println("------REFERENCE Assembly------")
            println(optimisedCode)
            println("------OUR OUTPUT------")
            println(assemblyCode)
        }
        ProcessBuilder("rm", "$name.s").start().waitFor(20, TimeUnit.SECONDS)
        assertTrue(success)
    }


    companion object {
        @JvmStatic
        fun testFiles(): List<File> {
            return getEachFile(File("wacc_examples/valid/optimisations"))
        }
    }

    private fun getAssemblyMap(): HashMap<String, String> {
        val map = HashMap<String, String>()
        val root = "wacc_examples/valid/optimisations"
        for (file in getEachFile(File(root))) {
            val assemblyFile =
                "reference_output/" +
                        file.invariantSeparatorsPath.split(".wacc").first()
                            .split("wacc_examples/valid/")
                            .last() + "/assembly.txt"
            val assemblyText = File(assemblyFile).inputStream().bufferedReader().use { it.readText() }
            map[file.nameWithoutExtension] = assemblyText
        }
        return map
    }
}