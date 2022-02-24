package backend.instruction

import backend.enums.Register

class PushInstruction (private val register : Register) : Instruction {
    override fun toString(): String {
        return "PUSH {$register}"
    }
}