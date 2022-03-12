import java.io.File

/**
 * Recursively finds each file in a given directory
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