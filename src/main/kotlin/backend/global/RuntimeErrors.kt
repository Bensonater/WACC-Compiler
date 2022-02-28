package backend.global

import backend.instruction.GeneralLabel
import backend.instruction.Instruction

class RuntimeErrors {

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
}