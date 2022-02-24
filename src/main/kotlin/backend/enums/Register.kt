package backend.enums


enum class Register {

    R0, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12,
    SP, // Stack Pointer
    LR, // Link register
    PC, // Program Counter
    CPSR, // Current Program Status Register

    NONE;

    override fun toString(): String {
        return name.lowercase()
    }
}