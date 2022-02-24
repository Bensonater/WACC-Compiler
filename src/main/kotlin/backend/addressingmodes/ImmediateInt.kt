package backend.addressingmodes

class ImmediateInt (val num : Int) : AddressingMode {
    override fun toString(): String {
        return "=$num"
    }
}