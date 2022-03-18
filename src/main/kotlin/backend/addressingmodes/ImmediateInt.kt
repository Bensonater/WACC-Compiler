package backend.addressingmodes

import backend.Language
import LANGUAGE

class ImmediateInt (val num : Int) : AddressingMode {
    override fun toString(): String {
        return when (LANGUAGE) {
            Language.ARM -> "=$num"
            Language.X86_64 -> "$$num"
        }
    }
}