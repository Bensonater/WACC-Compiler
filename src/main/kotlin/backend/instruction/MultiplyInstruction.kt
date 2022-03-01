package backend.instruction

import backend.enums.Condition
import backend.enums.Register

class MultiplyInstruction (val condition: Condition, val rdLo: Register, val rdHi: Register,
                           val rn: Register, val rm: Register) : Instruction {
    override fun toString(): String {
        return "SMULL$condition $rdLo, $rdHi, $rn, $rm"
    }
}