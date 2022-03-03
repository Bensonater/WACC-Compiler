package backend.instruction

import backend.enums.Register
import backend.addressingmodes.AddressingMode

enum class ArithmeticInstrType {
    ADD,
    SUB,
    RSB
}


class ArithmeticInstruction (val type: ArithmeticInstrType, val reg1: Register, val reg2: Register, val operand: AddressingMode, val update: Boolean = false) : Instruction {
    override fun toString(): String {
        return "${type.name}${if (update) "S" else ""} $reg1, $reg2, $operand"
    }
}