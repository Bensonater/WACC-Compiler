package backend.instruction

import backend.Language
import backend.enums.Register
import LANGUAGE

class PushInstruction (private val register : Register) : Instruction {
    override fun toString(): String {
        return when (LANGUAGE) {
            Language.ARM -> "PUSH {$register}"
            Language.X86_64 -> "pushq $register"
        }
    }
}