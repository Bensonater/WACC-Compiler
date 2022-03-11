package backend.instruction

import backend.Language
import language

class DirectiveInstruction (private val directive: String) : Instruction{
    override fun toString(): String {
        return when (language) {
            Language.ARM -> ".$directive"
            Language.X86_64 -> ".$directive"
        }
    }
}