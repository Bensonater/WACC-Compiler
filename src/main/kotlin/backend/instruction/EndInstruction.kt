package backend.instruction

import backend.Language
import backend.enums.Register
import language

class EndInstruction : Instruction {

    override fun toString(): String {
        return when (language) {
            Language.ARM -> "POP {${Register.PC}}"
            Language.X86_64 -> "leave\n\tret"
        }
    }
}