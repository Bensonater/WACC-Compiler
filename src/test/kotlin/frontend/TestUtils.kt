package frontend

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
}