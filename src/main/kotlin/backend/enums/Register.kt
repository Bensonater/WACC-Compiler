package backend.enums

import backend.Language
import language


enum class Register {

    R0, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12,
    SP, // Stack Pointer
    LR, // Link register
    PC, // Program Counter

    NONE;

    override fun toString(): String {
        return when (language) {
            Language.ARM -> name.lowercase()
            Language.X86_64 -> {
                "%" +
                when (this) {
                    R0 -> "rax"
                    R1 -> "rdi"
                    R2 -> "rsi"
                    R3 -> "rdx"
                    R4 -> "rdi"
                    R5 -> "rsi"
                    R6 -> "rdx"
                    R7 -> "rcx"
                    R8 -> "r8"
                    R9 -> "r9"
                    R10 -> "r12"
                    R11 -> "r13"
                    R12 -> "r14"
                    SP -> "rsp"
                    LR -> "rbp"
                    PC -> "rbp"
                    NONE -> ""
                }
            }
        }
    }
}