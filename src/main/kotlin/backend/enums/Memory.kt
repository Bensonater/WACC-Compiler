package backend.enums

/**
 * An enum for memory type sizes that can be associated with instruction
 */
enum class Memory {
    B,  // Unsigned Byte
    BT, // Byte with User mode privilege
    SB, // Signed Byte
    H,  // Unsigned Half Word
    SH, // Signed Half Word
    D   // Double Word
}