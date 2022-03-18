package backend.instruction

import backend.Language
import backend.addressingmodes.AddressingMode
import backend.enums.Memory
import backend.enums.Register
import LANGUAGE

class StoreInstruction (val mode: AddressingMode, val reg: Register, val memory: Memory? = null): Instruction {
    override fun toString(): String {
        return when (LANGUAGE) {
            Language.ARM -> "STR${memory?.name ?: ""} $reg, $mode"
            Language.X86_64 -> "mov${memory?.name?.lowercase()?.last() ?: ""} ${memory?.getRegType(reg) ?: reg}, " +
                    "$mode"
        }
    }
}

class StoreRegInstruction (val reg1: Register, val reg2: Register, val memory: Memory? = null): Instruction {
    override fun toString(): String {
        return when (LANGUAGE) {
            Language.ARM -> "STR${memory?.name ?: ""} $reg1, $reg2"
            Language.X86_64 -> "mov${memory?.name?.lowercase()?.last() ?: ""} ${memory?.getRegType(reg1) ?: reg1}, " +
                    "${memory?.getRegType(reg2) ?: reg2}"
        }
    }
}