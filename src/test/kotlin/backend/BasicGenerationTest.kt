package backend

import frontend.errors.SUCCESS_CODE
import org.antlr.v4.runtime.CharStreams
import org.junit.jupiter.api.Test
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class BasicGenerationTest {
    val root = "wacc_examples/valid/basic/skip/skip.wacc"

    @Test
    fun assemblyIsFunctionallyCorrect() {
        Runtime.getRuntime().exec("./compile $root")
        Runtime.getRuntime().exec("arm-linux-gnueabi-gcc -o skip -mcpu=arm1176jzf-s -mtune=arm1176jzf-s skip.s")
        val process2 = Runtime.getRuntime().exec("qemu-arm -L /usr/arm-linux-gnueabi skip")
        process2.inputStream.reader(Charsets.UTF_8).use {
            println(it.readText())
        }
        process2.waitFor(5, TimeUnit.SECONDS)
        println(process2.exitValue())
    }

}