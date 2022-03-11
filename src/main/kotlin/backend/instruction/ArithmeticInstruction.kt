package backend.instruction

import backend.Language
import backend.enums.Register
import backend.addressingmodes.AddressingMode
import language

enum class ArithmeticInstrType {
    ADD,
    SUB,
    RSB;

    override fun toString(): String {
        return when (language) {
            Language.ARM -> name
            Language.X86_64 -> name.lowercase()
        }
    }
}


class ArithmeticInstruction (val type: ArithmeticInstrType, val reg1: Register, val reg2: Register, val operand: AddressingMode, val update: Boolean = false) : Instruction {
    override fun toString(): String {
        return when (language) {
            Language.ARM -> "$type${if (update) "S" else ""} $reg1, $reg2, $operand"
            Language.X86_64 -> "$type $operand, $reg1"
        }

    }
}