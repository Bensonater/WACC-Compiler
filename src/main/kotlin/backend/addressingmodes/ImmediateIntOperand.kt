package backend.addressingmodes

import backend.Language
import language


class ImmediateIntOperand (val num : Int): AddressingMode {
    override fun toString(): String {
        return when (language) {
            Language.ARM -> "#$num"
            Language.X86_64 -> "$$num"
        }
    }
}
