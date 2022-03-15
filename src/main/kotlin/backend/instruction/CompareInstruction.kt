package backend.instruction

import backend.Language
import backend.addressingmodes.AddressingMode
import backend.enums.Register
import language

class CompareInstruction (val reg: Register, val operand: AddressingMode): Instruction {
    override fun toString(): String {
        return when (language) {
            Language.ARM -> "CMP $reg, $operand"
            Language.X86_64 -> "cmp $operand, $reg"
        }
    }
}