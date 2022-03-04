package backend

import getEachFile
import java.io.File

object ExampleFileOutputMap {
    val map = HashMap<String, Pair<String, Int>>()

    init {
        for (file in getEachFile(File("wacc_examples/valid/"))) {
            val fileName =
                file.invariantSeparatorsPath.split(".wacc").first().split("wacc_examples/valid/")
                    .last()
            val outputFile = File("reference_output/$fileName/output.txt")
            val errorFile = File("reference_output/$fileName/error.txt")

            val output =
                if (outputFile.exists()) {
                    outputFile.inputStream().bufferedReader().use { it.readText() }
                } else {
                    ""
                }
            val exitCode =
                if (errorFile.exists()) {
                    errorFile.inputStream().bufferedReader().use { it.readText() }.toInt()
                } else {
                    0
                }
            map[file.nameWithoutExtension] = Pair(output, exitCode)
        }
    }
}
