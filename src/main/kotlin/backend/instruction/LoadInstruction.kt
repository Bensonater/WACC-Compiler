package backend.instruction

import backend.addressingmodes.AddressingMode
import backend.enums.Register

class LoadInstruction (val addressingMode: AddressingMode, val register: Register) : Instruction {
    override fun toString(): String {
        return "LDR $register, $addressingMode"
    }
}