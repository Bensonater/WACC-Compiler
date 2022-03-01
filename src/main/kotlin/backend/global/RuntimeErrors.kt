package backend.global

import backend.ProgramState
import backend.addressingmodes.ImmediateIntOperand
import backend.addressingmodes.ImmediateLabel
import backend.addressingmodes.RegisterMode
import backend.addressingmodes.RegisterOperand
import backend.enums.Condition
import backend.enums.Register
import backend.instruction.*

class RuntimeErrors(val globalVals: ProgramState.GlobalVals) {

    private val EXIT_CODE = -1
    private var runtimeError: List<Instruction>? = null
    private var nullReferenceError: List<Instruction>? = null
    private var divideZeroError: List<Instruction>? = null
    private var checkArrayBounds: List<Instruction>? = null
    private var overflowError: List<Instruction>? = null

    companion object {
        val checkArrayBoundsLabel = GeneralLabel("p_check_array_bounds")
        val divideZeroCheckLabel = GeneralLabel("p_check_divide_by_zero")
        val exitLabel = GeneralLabel("exit")
        val nullReferenceLabel = GeneralLabel("p_check_null_pointer")
        val throwOverflowErrorLabel = GeneralLabel("p_throw_overflow_error")
        val throwRuntimeErrorLabel = GeneralLabel("p_throw_runtime_error")
    }

    enum class ErrorType(val msg: String) {
        NULL_REFERENCE("NullReferenceError: dereference a null reference\\n\\0"),
        DIVIDE_BY_ZERO("DivideByZeroError: divide or modulo by zero\\n"),
        LARGE_ARRAY_INDEX_OUT_OF_BOUNDS("ArrayIndexOutOfBoundsError: index too large\\n\\0"),
        NEGATIVE_ARRAY_INDEX_OUT_OF_BOUNDS("ArrayIndexOutOfBoundsError: negative index\\n\\0"),
        OVERFLOW_ERROR("OverflowError: the result is too small/large to store in a 4-byte signed-integer.\\n");

        override fun toString(): String {
            return msg
        }
    }

    fun translate(): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        runtimeError?.let { instructions.addAll(it) }
        nullReferenceError?.let { instructions.addAll(it) }
        divideZeroError?.let { instructions.addAll(it) }
        checkArrayBounds?.let { instructions.addAll(it) }
        overflowError?.let { instructions.addAll(it) }
        return instructions
    }

    fun addThrowRuntimeError() {
        if (runtimeError == null) {
            runtimeError = listOf(
                throwRuntimeErrorLabel,
                BranchInstruction(Condition.AL, GeneralLabel(CallFunc.PRINT_STRING.toString()), true),
                MoveInstruction(Condition.AL, Register.R0, ImmediateIntOperand(EXIT_CODE)),
                BranchInstruction(Condition.AL, exitLabel, true)
            )
            globalVals.library.addCode(CallFunc.PRINT_STRING)
        }
    }

    fun addOverflowError() {
        if (overflowError == null) {
            val errorMsg = globalVals.dataDirective.addStringLabel(ErrorType.OVERFLOW_ERROR.toString())
            overflowError = listOf(
                throwOverflowErrorLabel,
                LoadInstruction(Condition.AL, ImmediateLabel(errorMsg), Register.R0),
                BranchInstruction(Condition.AL, throwRuntimeErrorLabel, true),
            )
        }
        addThrowRuntimeError()
    }

    fun addNullReferenceCheck() {
        if (nullReferenceError == null) {
            val errorMsgLabel = globalVals.dataDirective.addStringLabel(ErrorType.NULL_REFERENCE.toString())
            nullReferenceError = listOf(
                nullReferenceLabel,
                PushInstruction(Register.LR),
                CompareInstruction(Register.R0, ImmediateIntOperand(0)),
                LoadInstruction(Condition.EQ, ImmediateLabel(errorMsgLabel), Register.R0),
                BranchInstruction(Condition.EQ, throwRuntimeErrorLabel, true),
                PopInstruction(Register.PC)
            )
        }
        addThrowRuntimeError()
    }

    fun addDivideByZeroCheck() {
        if (divideZeroError == null) {
            val errorMsgLabel = globalVals.dataDirective.addStringLabel(ErrorType.DIVIDE_BY_ZERO.toString())
            divideZeroError = listOf(
                divideZeroCheckLabel,
                PushInstruction(Register.LR),
                CompareInstruction(Register.R1, ImmediateIntOperand(0)),
                LoadInstruction(Condition.EQ, ImmediateLabel(errorMsgLabel), Register.R0),
                BranchInstruction(Condition.EQ, throwRuntimeErrorLabel, true),
                PopInstruction(Register.PC)
            )
        }
        addThrowRuntimeError()
    }


    fun addArrayBoundsCheck() {
        if (checkArrayBounds == null) {
            val negativeMsgLabel = globalVals.dataDirective.addStringLabel(ErrorType.NEGATIVE_ARRAY_INDEX_OUT_OF_BOUNDS.toString())
            val tooLargeMsgLabel = globalVals.dataDirective.addStringLabel(ErrorType.LARGE_ARRAY_INDEX_OUT_OF_BOUNDS.toString())

            checkArrayBounds = listOf(
                checkArrayBoundsLabel,
                PushInstruction(Register.LR),
                CompareInstruction(Register.R0, ImmediateIntOperand(0)),
                LoadInstruction(Condition.LT, ImmediateLabel(negativeMsgLabel), Register.R0),
                BranchInstruction(Condition.LT, throwRuntimeErrorLabel, true),
                LoadInstruction(Condition.AL,  RegisterMode(Register.R1), Register.R1),
                CompareInstruction(Register.R0, RegisterOperand(Register.R1)),
                LoadInstruction(Condition.CS, ImmediateLabel(tooLargeMsgLabel), Register.R0),
                BranchInstruction(Condition.CS, throwRuntimeErrorLabel, true),
                PopInstruction(Register.PC)
            )
            addThrowRuntimeError()
        }
    }
}