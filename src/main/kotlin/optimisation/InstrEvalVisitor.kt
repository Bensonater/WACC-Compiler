package optimisation

import backend.instruction.ArithmeticInstruction
import backend.instruction.Instruction
import backend.instruction.LoadInstruction
import backend.instruction.StoreInstruction

class InstrEvalVisitor(val instructions: MutableList<Instruction>) {
    fun optimise(): List<Instruction> {
        return instructions
    }

    fun optimiseStoreThenLoad(): List<Instruction> {
//        val iterator = instructions.listIterator()
//        var prev = instructions.first()
//        while (iterator.hasNext()){
//            val curr = iterator.next()
//            if (prev is StoreInstruction && curr is LoadInstruction && prev.reg == curr.register) {
//                iterator.remove()
//            }
//            prev = iterator.previous()
//        }
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