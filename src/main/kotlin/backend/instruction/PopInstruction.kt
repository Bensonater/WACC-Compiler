package backend.instruction

import backend.Language
import backend.enums.Register
import language

class PopInstruction (private val register : Register) : Instruction {
    override fun toString(): String {
        return when (language) {
            Language.ARM -> "POP {$register}"
            Language.X86_64 -> "popq $register"
        }
    }
}