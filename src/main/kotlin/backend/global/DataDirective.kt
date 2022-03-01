package backend.global

import backend.instruction.DirectiveInstruction
import backend.instruction.Instruction
import backend.instruction.MessageLabel

class DataDirective {
    val dataLabels = mutableListOf<String>()

    fun addStringLabel(stringLabel: String) : String {
        return if(dataLabels.contains(stringLabel)) {
            "msg_${dataLabels.indexOf(stringLabel)}"
        } else {
            dataLabels.add(stringLabel)
            "msg_${dataLabels.size - 1}"
        }
    }

    fun toStringLabel(string: String): String {
        return "msg_${dataLabels.indexOf(string)}"
    }

    fun translate() : List<Instruction> {
        if (dataLabels.isEmpty()) {
            return emptyList()
        }

        val instructions = mutableListOf<Instruction>(DirectiveInstruction("data"))
        for ((index, label) in dataLabels.withIndex()) {
            instructions.add(MessageLabel(index, label))
        }
        return instructions
    }
}