package backend.instruction

import backend.addressingmodes.AddressingMode
import backend.enums.Memory
import backend.enums.Register

class StoreInstruction (val mode: AddressingMode, val reg: Register, val memory: Memory? = null): Instruction {
    override fun toString(): String {
        return "STR${memory?.name ?: ""} $reg, $mode"
    }
}