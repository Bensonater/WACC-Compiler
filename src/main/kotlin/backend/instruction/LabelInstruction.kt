package backend.instruction

abstract class LabelInstruction (val labelName : String) : Instruction {
    override fun toString(): String {
        return "$labelName:"
    }
}

class GeneralLabel(labelName: String) : LabelInstruction(labelName)

class FunctionLabel(functionName: String) : LabelInstruction("f_$functionName")

data class MessageLabel(val index: Int, val msg: String) : LabelInstruction(msg) {
    override fun toString(): String {
        val stringInstructions = mutableListOf<String>()
        stringInstructions.add(GeneralLabel("msg_$index").toString())
        stringInstructions.add("\t${DirectiveInstruction("word ${msg.length - msg.count { c -> c == '\\' }}")}")
        stringInstructions.add("\t${DirectiveInstruction("ascii \"${msg}\"")}")
        return stringInstructions.joinToString(separator = "\n")
    }

}