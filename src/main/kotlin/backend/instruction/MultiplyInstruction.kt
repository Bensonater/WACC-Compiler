package backend.instruction

import backend.Language
import backend.addressingmodes.AddressingMode
import backend.enums.Condition
import backend.enums.Register
import language

class MultiplyInstruction (val condition: Condition, val rdLo: Register, val rdHi: Register,
                           val rn: Register, val rm: Register, val operand: AddressingMode? = null) : Instruction {
    override fun toString(): String {
        return when (language) {
            Language.ARM -> "SMULL$condition $rdLo, $rdHi, $rn, $rm"
            Language.X86_64 -> "imul  $operand, $rdLo"
        }
    }
}