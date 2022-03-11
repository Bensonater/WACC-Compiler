package backend.addressingmodes

import backend.Language
import language

class ImmediateLabel (val label : String): AddressingMode {
    override fun toString(): String {
        return when (language) {
            Language.ARM -> "=$label"
            Language.X86_64 -> label
        }
    }
}