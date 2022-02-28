package backend.instruction

import backend.addressingmodes.AddressingMode
import backend.enums.Condition
import backend.enums.Register

class MoveInstruction (val condition: Condition, val reg: Register, val value: AddressingMode) : Instruction {
    override fun toString(): String {
        return "MOV$condition $reg, $value"
    }

}