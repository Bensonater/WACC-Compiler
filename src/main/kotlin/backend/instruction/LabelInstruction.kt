package backend.instruction

abstract class LabelInstruction (private val labelName : String) : Instruction {
    override fun toString(): String {
        return "$labelName:"
    }
}

class GeneralLabel(private val labelName: String) : LabelInstruction(labelName)

class FunctionLabel(private val functionName: String) : LabelInstruction("f_$functionName")

data class MessageLabel(val index: Int, val msg: String) : LabelInstruction(msg) {
    override fun toString(): String {
        TODO("Implement message label")
    }

}