package backend.instruction

class DirectiveInstruction (private val directive: String) : Instruction{
    override fun toString(): String {
        return ".$directive"
    }
}