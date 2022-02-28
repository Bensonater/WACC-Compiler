package backend.addressingmodes

import backend.enums.Register

class RegisterOperand (val register : Register) : AddressingMode {
    override fun toString(): String {
        return register.toString()
    }
}