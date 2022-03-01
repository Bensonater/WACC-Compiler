package backend.instruction

import backend.addressingmodes.AddressingMode
import backend.enums.Condition
import backend.enums.Memory
import backend.enums.Register

class LoadInstruction (val condition: Condition, val addressingMode: AddressingMode, val register: Register, val memoryType: Memory? = null) : Instruction {
    override fun toString(): String {
        return "LDR${memoryType?.name ?: ""}$condition $register, $addressingMode"
    }
}