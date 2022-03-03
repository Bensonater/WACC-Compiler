package backend

import frontend.TestUtils
import org.junit.Ignore
import java.io.File

class CodeFunctionalityTest : TestUtils {


    private fun mapOutputsAndErrorCodes(): HashMap<String, Pair<String, Int>> {
        val root = "reference_output/"
        val map = HashMap<String, Pair<String, Int>>()

        doForEachFile(File(root)) { file ->
            val inputStream = file.inputStream()
            val inputString = inputStream.bufferedReader().use { it.readText() }
            val parts = inputString.split("===========================================================\n")

            map.put(file.nameWithoutExtension, Pair(parts.first(), parts.last().toInt()))
        }
        return map
    }

    @Ignore
    fun assemblyIsFunctionallyCorrect() {
        val map = mapOutputsAndErrorCodes()
        val root = "wacc_examples/valid/"

        var passing = 0

        doForEachFile(File(root)) { file ->
            val name = file.nameWithoutExtension
            val path = file.invariantSeparatorsPath
            var process = ProcessBuilder("./compile", path).start()
            if (map[name] == null || name == "printTriangle") {
                return@doForEachFile
            }
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
            process.waitFor()

            var success: Boolean
            process.inputStream.reader(Charsets.UTF_8).use {
                success = refOutput == it.readText()
            }

            success = success && (refError == process.exitValue())

            if (success) {
                passing++
            } else {
                println(name)
            }

            process = ProcessBuilder("rm", "$name.s", name).start()
            process.waitFor()
            println(passing)
        }

    }
}