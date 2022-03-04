package frontend

import java.io.File

interface TestUtils {
    /**
     * Recursively finds each file in the specified directory
     */
    fun getEachFile(file: File): List<File> {
        val listOfFiles = emptyList<File>().toMutableList()
        if (file.isDirectory) {
            for (subFile in file.listFiles()!!) {
                listOfFiles += getEachFile(subFile)
            }
        } else {
            listOfFiles += file
        }
        return listOfFiles
    }
}