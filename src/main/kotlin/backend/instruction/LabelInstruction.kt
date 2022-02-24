package backend.instruction

abstract class LabelInstruction (private val labelName : String) : Instruction {
    override fun toString(): String {
        return "$labelName:"
    }
}

data class GeneralLabel(private val labelName: String) : LabelInstruction(labelName)

data class FunctionLabel(private val functionName: String) : LabelInstruction("f_$functionName")

data class MessageLabel(val index: Int, val msg: String) : LabelInstruction(msg) {
    override fun toString(): String {
        TODO("Implement message label")
    }

}