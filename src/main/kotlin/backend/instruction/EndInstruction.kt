package backend.instruction

import backend.Register

class EndInstruction : Instruction {

    override fun toString(): String {
        return "POP {${Register.PC}}"
    }
}