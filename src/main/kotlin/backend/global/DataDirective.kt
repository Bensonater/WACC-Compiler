package backend.global

import backend.instruction.DirectiveInstruction
import backend.instruction.Instruction
import backend.instruction.MessageLabel

class DataDirective {
    val dataLabels = emptyList<String>()

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