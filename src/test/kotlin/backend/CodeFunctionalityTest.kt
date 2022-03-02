package backend

import frontend.TestUtils
import org.junit.Ignore
import org.junit.jupiter.api.Test
import java.io.File
import java.util.concurrent.TimeUnit

class CodeFunctionalityTest : TestUtils {
    private val root = "wacc_examples/valid/"

    @Ignore
    fun assemblyIsFunctionallyCorrect() {
        doForEachFile(File(root)) { file ->
            val name = file.nameWithoutExtension
            val path = file.invariantSeparatorsPath
            //  println(name)
            //  println(path)

            Runtime.getRuntime().exec("./compile $path")
            Runtime.getRuntime()
                .exec("arm-linux-gnueabi-gcc -o $name -mcpu=arm1176jzf-s -mtune=arm1176jzf-s $name.s")
            val process2 = Runtime.getRuntime().exec("qemu-arm -L /usr/arm-linux-gnueabi $name")
            process2.inputStream.reader(Charsets.UTF_8).use {
                println(it.readText())
            }
            process2.waitFor(5, TimeUnit.SECONDS)
            println(process2.exitValue())
            Runtime.getRuntime().exec("rm $name.s")
        }


    }
}