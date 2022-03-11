package backend.instruction

import backend.Language
import backend.addressingmodes.AddressingMode
import backend.enums.Register
import language

enum class LogicOperation {
    AND,
    ORR,
    EOR
}

class LogicInstruction(val op: LogicOperation, val reg1: Register, val reg2: Register,
                       val operand: AddressingMode) : Instruction {
    override fun toString(): String {
        return when (language) {
            Language.ARM -> "${op.name} $reg1, $reg2, $operand"
            Language.X86_64 -> "${op.name} $reg2, $operand"
        }
    }
}