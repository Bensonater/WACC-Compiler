package backend.instruction

abstract class LabelInstruction (private val labelName : String) : Instruction {
    override fun toString(): String {
        return "$labelName:"
    }
}

class GeneralLabel(labelName: String) : LabelInstruction(labelName)

class FunctionLabel(functionName: String) : LabelInstruction("f_$functionName")