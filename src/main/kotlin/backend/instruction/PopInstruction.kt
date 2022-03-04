package backend.instruction

import backend.enums.Register

class PopInstruction (private val register : Register) : Instruction {
    override fun toString(): String {
        return "POP {$register}"
    }
}