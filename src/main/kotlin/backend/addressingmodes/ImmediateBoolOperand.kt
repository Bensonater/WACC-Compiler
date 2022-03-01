package backend.addressingmodes

class ImmediateBoolOperand(val boolVal: Boolean): AddressingMode {
    override fun toString(): String {
        return "#${if (boolVal) 1 else 0}"
    }
}