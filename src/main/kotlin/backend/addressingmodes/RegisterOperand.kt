package backend.addressingmodes

import backend.Language
import backend.enums.Register
import language

class RegisterOperand (val register : Register) : AddressingMode {
    override fun toString(): String {
        return when (language) {
            Language.ARM -> register.toString()
            Language.X86_64 -> register.toString()
        }
    }
}

class RegisterOperandWithShift (val register: Register, val shiftType: ShiftType, val offset: Int) : AddressingMode {
    override fun toString(): String {
        return when (language) {
            Language.ARM -> "$register, $shiftType #$offset"
            Language.X86_64 -> "$shiftType $$offset, $register"
        }
    }
}

enum class ShiftType {
    ASR,
    LSL;

    override fun toString(): String {
        return when (language) {
            Language.ARM -> name
            Language.X86_64 -> {
                when (this) {
                    ASR -> "shr"
                    LSL -> "shl"
                }
            }
        }
    }
}