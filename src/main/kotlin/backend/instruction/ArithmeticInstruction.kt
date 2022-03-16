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
            Language.X86_64 -> {
                if (type == ArithmeticInstrType.RSB) {
                    return "neg $reg1"
                }
                var result = ""
                if (reg1 != reg2) {
                    result += "mov $reg2, $reg1\n\t"
                }
                result += "$type $operand, $reg1"
                return result
            }

        }

    }
}