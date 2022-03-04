package backend

import java.io.File

interface TestUtils {
    /**
     * Recursively finds each file in the specified directory, performing the given
     * operation on them and returning a list of the results
     */
    fun <T> doForEachFile(file: File, operation: (File) -> T): List<T> {
        val operatedList = emptyList<T>().toMutableList()
        if (file.isDirectory) {
            for (subFile in file.listFiles()!!) {
                operatedList += doForEachFile(subFile, operation)
            }
        } else {
            operatedList += operation(file)
        }
        return operatedList
    }

    fun mapOutputsAndErrorCodes(): HashMap<String, Pair<String, Int>> {
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
}