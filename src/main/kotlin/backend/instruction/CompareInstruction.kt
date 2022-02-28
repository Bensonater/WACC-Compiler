package backend.instruction

import backend.addressingmodes.AddressingMode
import backend.enums.Register

class CompareInstruction (val reg: Register, val operand: AddressingMode): Instruction {
    override fun toString(): String {
        return "CMP $reg, $operand"
    }
}