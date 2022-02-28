package backend.instruction

import backend.enums.Condition

class BranchInstruction (val condition: Condition, val label: LabelInstruction, val link: Boolean): Instruction {
    override fun toString(): String {
        return "B" + (if(link) "L" else "")  + condition.toString() + " " + label.labelName
    }
}