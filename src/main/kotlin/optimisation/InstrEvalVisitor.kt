package optimisation

import backend.instruction.Instruction
import backend.instruction.LoadInstruction
import backend.instruction.StoreInstruction

class InstrEvalVisitor {
    fun optimise(instructions: List<Instruction>): List<Instruction> {
        var optimised = instructions
        return optimiseStoreThenLoad(optimised)
    }

    private fun optimiseStoreThenLoad(instructions: List<Instruction>): List<Instruction> {
        var prev = instructions.first()
        val optimised = mutableListOf<Instruction>()
        for (i in instructions) {
            if (prev is StoreInstruction && i is LoadInstruction && prev.reg == i.register) {
            } else {
                optimised.add(i)
            }
            prev = i
        }
        return optimised
    }

//    fun optimiseAddZero(){
//        val addInstr = instructions.filterNot { it is ArithmeticInstruction && it.type is  }
//        val
//
//    }

}