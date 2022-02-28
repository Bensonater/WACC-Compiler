package backend.addressingmodes

class ImmediateLabel (val label : String): AddressingMode {
    override fun toString(): String {
        return "=$label"
    }
}