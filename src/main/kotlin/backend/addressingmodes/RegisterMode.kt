package backend.addressingmodes

import backend.enums.Register

class RegisterMode (val reg : Register) : AddressingMode {
    override fun toString(): String {
        return "[$reg]"
    }
}

class RegisterModeWithOffset(val reg: Register, val offset : Int, val preIndex: Boolean = false) : AddressingMode {
    override fun toString(): String {
        return "[$reg${if (offset != 0) ", #${offset}" else ""}]${if (preIndex) "!" else ""}"
    }
}