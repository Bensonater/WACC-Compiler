package backend.instruction

import backend.ProgramState
import backend.SIZE_OF_POINTER
import backend.enums.Register

class PushInstruction (private val register : Register) : Instruction {
    init {
        ProgramState.stackPointer += SIZE_OF_POINTER
    }

    override fun toString(): String {
        return "PUSH {$register}"
    }
}