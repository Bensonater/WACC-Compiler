package backend.addressingmodes

import backend.ProgramState
import backend.enums.Register

class RegisterMode (val reg : Register) : AddressingMode {
    override fun toString(): String {
        return "[$reg]"
    }
}

class RegisterModeWithOffset(val reg: Register, val offset : Int, val preIndex: Boolean = false) : AddressingMode {
    init {
        if (preIndex) {
            ProgramState.stackPointer -= offset
        }
    }

    override fun toString(): String {
        return "[$reg${if (offset != 0) ", #${offset}" else ""}]${if (preIndex) "!" else ""}"
    }
}