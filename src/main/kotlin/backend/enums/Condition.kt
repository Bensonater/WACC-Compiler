package backend.enums

import backend.Language
import LANGUAGE

enum class Condition {
    EQ,
    NE,
    GT,
    GE,
    LT,
    LE,
    CS,
    VS,
    AL;

    override fun toString(): String {
        return when (LANGUAGE) {
            Language.ARM -> {
                if (this == AL) {
                    ""
                } else {
                    name
                }
            }
            Language.X86_64 -> {
                when (this) {
                    EQ -> "e"
                    NE -> "ne"
                    GT -> "g"
                    GE -> "ge"
                    LT -> "l"
                    LE -> "le"
                    CS -> "ae"
                    VS -> "o"
                    AL -> ""
                }
            }
        }
    }
}