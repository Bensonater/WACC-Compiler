package backend.instruction

import backend.Language
import backend.addressingmodes.AddressingMode
import backend.enums.Memory
import backend.enums.Register
import language

class StoreInstruction (val mode: AddressingMode, val reg: Register, val memory: Memory? = null): Instruction {
    override fun toString(): String {
        return when (language) {
            Language.ARM -> "STR${memory?.name ?: ""} $reg, $mode"
            Language.X86_64 -> "mov $reg, $mode"
        }
    }
}