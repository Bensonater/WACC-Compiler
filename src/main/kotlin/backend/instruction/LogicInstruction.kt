package backend.instruction

import backend.addressingmodes.AddressingMode
import backend.enums.Register

enum class LogicOperation {
    AND,
    OR,
    EOR
}

class LogicInstruction(val op: LogicOperation, val reg1: Register, val reg2: Register,
                       val operand: AddressingMode) : Instruction {
    override fun toString(): String {
        return "${op.name} $reg1, $reg2, $operand"
    }
}