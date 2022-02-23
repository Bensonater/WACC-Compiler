package backend.instruction

import backend.Register

class PushInstruction (private val register : Register) : Instruction {
    override fun toString(): String {
        return "PUSH {$register}"
    }
}