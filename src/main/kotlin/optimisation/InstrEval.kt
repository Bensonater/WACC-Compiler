package optimisation

import backend.addressingmodes.RegisterMode
import backend.instruction.Instruction
import backend.instruction.LoadInstruction
import backend.instruction.StoreInstruction

class InstrEval {
    fun optimise(instructions: List<Instruction>): List<Instruction> {
        val optimised = mutableListOf<Instruction>()

        var prev = instructions.first()
        for (i in instructions) {
            if (storeThenLoad(prev, i)) {
            } else {
                optimised.add(i)
            }
            prev = i
        }
        return optimised
    }

    private fun storeThenLoad(prev: Instruction, curr: Instruction): Boolean {
        return prev is StoreInstruction && curr is LoadInstruction && prev.reg == curr.register
                && prev.mode is RegisterMode && curr.addressingMode is RegisterMode && prev.mode.reg == curr.addressingMode.reg
    }

//    fun optimiseAddZero(instructions: List<Instruction>): List<Instruction>{
//        val addInstr = instructions.filterNot { it is ArithmeticInstruction && it.type is  }
//
//    }

}