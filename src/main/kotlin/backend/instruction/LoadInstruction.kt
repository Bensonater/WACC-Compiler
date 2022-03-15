package backend.instruction

import backend.Language
import backend.addressingmodes.AddressingMode
import backend.enums.Condition
import backend.enums.Memory
import backend.enums.Register
import language

class LoadInstruction (val condition: Condition, val addressingMode: AddressingMode, val register: Register, val memoryType: Memory? = null) : Instruction {
    override fun toString(): String {
        return when (language) {
            Language.ARM -> "LDR${memoryType?.name ?: ""}$condition $register, $addressingMode"
            Language.X86_64 -> "mov $addressingMode, $register"
        }
    }
}
