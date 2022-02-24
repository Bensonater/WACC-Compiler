package backend

import backend.enums.Register
import backend.global.DataDirective
import java.util.*

class ProgramState {

    companion object GlobalVals {
        val dataDirective = DataDirective()

    }

    var resultRegs: MutableList<Register> = mutableListOf(Register.R0, Register.R1)
    var argumentRegs: MutableList<Register> = mutableListOf(Register.R2, Register.R3)
    var freeCalleeSavedRegs: ArrayDeque<Register> = ArrayDeque<Register>(listOf(
        Register.R4, Register.R5,
        Register.R6, Register.R7, Register.R8, Register.R9, Register.R10, Register.R11, Register.R12))
    var inUseCalleeSavedRegs: ArrayDeque<Register> = ArrayDeque<Register>()

}