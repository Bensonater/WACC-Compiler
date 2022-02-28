package backend.instruction

import backend.addressingmodes.AddressingMode
import backend.enums.Condition
import backend.enums.Register

class LoadInstruction (val condition: Condition, val addressingMode: AddressingMode, val register: Register) : Instruction {
    override fun toString(): String {
        return "LDR$condition $register, $addressingMode"
    }
}