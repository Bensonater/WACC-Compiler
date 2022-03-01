package backend.addressingmodes

class ImmediateCharOperand(val char : Char) : AddressingMode {
    override fun toString(): String {
        val charStr: String = when (char) {
            0.toChar() -> "#0"
            12.toChar() -> "\\f"
            '\b' -> "\\b"
            '\t' -> "\\t"
            '\n' -> "\\n"
            '\r' -> "\\r"
            '\"' -> "\\\""
            '\'' -> "\\\'"
            '\\' -> "\\\\"
            else -> char.toString()
        }
        return "#'${charStr}'"
    }
}