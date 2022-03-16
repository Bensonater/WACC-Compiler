package backend.instruction

import backend.Language
import backend.addressingmodes.AddressingMode
import backend.enums.Condition
import backend.enums.Register
import language

class MultiplyInstruction (val condition: Condition, val rdLo: Register, val rdHi: Register,
                           val rn: Register, val rm: Register, val operand: AddressingMode? = null) : Instruction {
    override fun toString(): String {
        return "SMULL$condition $rdLo, $rdHi, $rn, $rm"
    }
}

/**
 * Multiples %rax with %{reg}
 */
class iMultiplyInstruction (val reg: Register) : Instruction {
    override fun toString(): String {
        return "imul $reg"
    }
}