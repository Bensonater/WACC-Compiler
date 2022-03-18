package backend.addressingmodes

import backend.Language
import LANGUAGE

class ImmediateLabel (val label : String): AddressingMode {
    override fun toString(): String {
        return when (LANGUAGE) {
            Language.ARM -> "=$label"
            Language.X86_64 -> label
        }
    }
}