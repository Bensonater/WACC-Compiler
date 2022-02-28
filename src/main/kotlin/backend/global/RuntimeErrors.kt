package backend.global

import backend.ProgramState
import backend.instruction.GeneralLabel
import backend.instruction.Instruction

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
//                BranchInstr(Condition.AL, Label(CLibrary.Call.PRINT_STRING.toString()), true),
//                MoveInstr(Condition.AL, Register.R0, ImmediateIntOperand(EXIT_CODE)),
//                BranchInstr(Condition.AL, exitLabel, true)
            )
//            globalVals.cLib.addCode(CLibrary.Call.PRINT_STRING)

        }
    }

}