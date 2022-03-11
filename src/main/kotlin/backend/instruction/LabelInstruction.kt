package backend.instruction

import backend.Language
import language

abstract class LabelInstruction (val labelName : String) : Instruction {
    override fun toString(): String {
        return when (language){
            Language.ARM -> "$labelName:"
            Language.X86_64 -> "$labelName:"
        }
    }
}

class GeneralLabel(labelName: String) : LabelInstruction(labelName)

class FunctionLabel(functionName: String) : LabelInstruction("f_$functionName")

data class MessageLabel(val index: Int, val msg: String) : LabelInstruction(msg) {
    override fun toString(): String {
        return when (language) {
            Language.ARM -> {
                val stringInstructions = mutableListOf<String>()
                stringInstructions.add(GeneralLabel("msg_$index").toString())
                stringInstructions.add("\t${DirectiveInstruction("word ${msg.length - msg.count { c -> c == '\\' }}")}")
                stringInstructions.add("\t${DirectiveInstruction("ascii \"${msg}\"")}")
                stringInstructions.joinToString(separator = "\n")
            }
            Language.X86_64 -> {
                val stringInstructions = mutableListOf<String>()
                stringInstructions.add(GeneralLabel("msg_$index").toString())
                stringInstructions.add("\t ${DirectiveInstruction("string \"$msg\"")}")
                stringInstructions.joinToString(separator = "\n")
            }
        }
    }

}