package backend.instruction

import backend.Register
import backend.addressingmodes.AddressingMode

enum class ArithmeticInstrType {
    ADD,
    SUB
}


class ArithmeticInstruction (val type: ArithmeticInstrType, val reg1: Register, val reg2: Register, val operand: AddressingMode) : Instruction {
    override fun toString(): String {
        return "${type.name} $reg1, $reg2, $operand"
    }
}