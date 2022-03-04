package backend

import backend.enums.Register
import backend.global.DataDirective
import backend.global.Library
import backend.global.RuntimeErrors
import backend.instruction.GeneralLabel
import java.util.*

class ProgramState {

    companion object GlobalVals {
        val dataDirective = DataDirective()
        val runtimeErrors = RuntimeErrors(this)
        val library = Library(this)
        var labelNum = 0
    }

//    val resultRegs: MutableList<Register> = mutableListOf(Register.R0, Register.R1)
//    val argumentRegs: MutableList<Register> = mutableListOf(Register.R2, Register.R3)
    val freeCalleeSavedRegs: ArrayDeque<Register> = ArrayDeque<Register>(listOf(
        Register.R4, Register.R5,
        Register.R6, Register.R7, Register.R8, Register.R9, Register.R10, Register.R11))
    val inUseCalleeSavedRegs: ArrayDeque<Register> = ArrayDeque<Register>()

    var accumulatorUsed = false

    fun freeAllCalleeRegs() {
        while (inUseCalleeSavedRegs.isNotEmpty()) {
            freeCalleeReg()
        }
    }

    /**
     * Free most recently used callee reg.
     */
    fun freeCalleeReg() {
        if (inUseCalleeSavedRegs.isEmpty()) {
            return
        }
        freeCalleeSavedRegs.push(inUseCalleeSavedRegs.pop())
    }

    fun recentlyUsedCalleeReg() : Register {
        return if (accumulatorUsed) {
            accumulatorUsed = false
            Register.NONE
        } else {
            inUseCalleeSavedRegs.peek()
        }
    }

    fun getFreeCalleeReg() : Register {
        return if (freeCalleeSavedRegs.isEmpty()) {
            accumulatorUsed = true
            Register.NONE
        } else {
            val reg = freeCalleeSavedRegs.pop()
            inUseCalleeSavedRegs.push(reg)
            reg
        }

    }

    /** Gets the next free label number using a global counter */
    fun getNextLabel(): GeneralLabel {
        return GeneralLabel("L${labelNum++}")
    }
}