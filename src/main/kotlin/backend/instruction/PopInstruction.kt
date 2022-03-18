package backend.instruction

import backend.Language
import backend.enums.Register
import LANGUAGE

class PopInstruction (private val register : Register) : Instruction {
    override fun toString(): String {
        return when (LANGUAGE) {
            Language.ARM -> "POP {$register}"
            Language.X86_64 -> "popq $register"
        }
    }
}