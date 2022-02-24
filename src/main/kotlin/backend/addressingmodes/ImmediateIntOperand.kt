package backend.addressingmodes 


class ImmediateIntOperand (val num : Int): AddressingMode {
    override fun toString(): String {
        return "#$num"
    }
}
