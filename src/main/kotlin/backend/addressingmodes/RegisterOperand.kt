package backend.addressingmodes

import backend.enums.Register

class RegisterOperand (val register : Register) : AddressingMode {
    override fun toString(): String {
        return register.toString()
    }
}

class RegisterOperandWithShift (val register: Register, val shiftType: ShiftType, val offset: Int) : AddressingMode {
    override fun toString(): String {
        return "$register, $shiftType #$offset"
    }
}

enum class ShiftType {
    ASR,
    LSL,
    LSR,
    ROR,
    RRX
}