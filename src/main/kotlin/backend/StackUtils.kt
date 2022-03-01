package backend

import backend.addressingmodes.ImmediateIntOperand
import backend.enums.Register
import backend.instruction.ArithmeticInstrType
import backend.instruction.ArithmeticInstruction
import backend.instruction.Instruction
import frontend.FuncSymbolTable
import frontend.SymbolTable
import frontend.ast.ParamAST
import frontend.ast.statement.DeclareAST

private const val MAX_STACK_OFFSET = 1024

fun calculateStackOffset(symbolTable : SymbolTable) : Int {
    var offset = 0
    for (astNode in symbolTable.symbolTable.values) {
        if (astNode is DeclareAST) {
            offset += astNode.size // Potential optimisation to store offset in symbol table
        }
    }
    return offset
}

fun allocateStack (symbolTable: SymbolTable, instructions: MutableList<Instruction>) : Int {
    val stackOffset = calculateStackOffset(symbolTable)
    symbolTable.startingOffset = stackOffset
    moveStackPointer(ArithmeticInstrType.SUB, stackOffset, instructions)
    return stackOffset
}

fun deallocateStack (stackOffset: Int, instructions: MutableList<Instruction>) {
    moveStackPointer(ArithmeticInstrType.ADD, stackOffset, instructions)
}

fun moveStackPointer (addOrSubtract: ArithmeticInstrType, stackOffset: Int,
                              instructions: MutableList<Instruction>) {
    if (stackOffset > 0) {
//        while (stackOffsetLeft > MAX_STACK_OFFSET) { // Why?
//            instructions.add(
//                ArithmeticInstruction(addOrSubtract, Register.SP, Register.SP,
//                ImmediateIntOperand(MAX_STACK_OFFSET)
//                )
//            )
//            stackOffsetLeft -= MAX_STACK_OFFSET
//        }
        instructions.add(
            ArithmeticInstruction(addOrSubtract, Register.SP, Register.SP,
            ImmediateIntOperand(stackOffset)
            )
        )
    }
}

/**
 * Finds the offset in stack for identifier
 * @param symbolTable The symbolTable of the current scope
 * @param ident The name of the variable
 * @return The offset in the stack for the variable
 *
 * Potential optimisation to store offset in symbol table
 */
fun findIdentOffset(symbolTable: SymbolTable, ident: String): Int {
    var totalOffset = symbolTable.symbolTable.values.sumOf { it.size }
    val pointerOffset = 4
    var paramOffset = 0
    for ((key, node) in symbolTable.symbolTable) {
        if (key == ident && node is ParamAST) {
            return paramOffset + pointerOffset
        }
        totalOffset -= node.size
        if (key == ident) {
            return totalOffset
        }
        paramOffset += node.size
    }
    if (symbolTable.parent != null) {
        /** Searches parent symbol table when not found in current scope.
         * Includes addition of totalOffset size of current scope */
        return findIdentOffset(symbolTable.parent!!, ident) + paramOffset
    }
    return totalOffset
}


/**
 * Recursive method to check if identifier is a parameter and returns offset on stack if so
 *
 * @param symbolTable Symbol Table of the current scope
 * @param ident Identifier of the potential parameter
 * @param innerScopeHaveVar Have variables been declared inside the function scope
 * @param offsetCount Accumulative offset until parameter is found
 * @return The offset of the parameter on the stack
 */
private fun findParamInFuncOffset(symbolTable: SymbolTable, ident: String, innerScopeHaveVar: Boolean,
                                  offsetCount: Int): Int {
    val identAst = symbolTable.get(ident)
    if (symbolTable is FuncSymbolTable && identAst is ParamAST) {
        // Parameter offset only needed when there are variables declared in the current scope or any inner scope
//        if ((symbolTable.symbolTable.size > symbolTable.funcAST.paramList.size) || innerScopeHaveVar) {
//            // Sum offset of all variables that's not a parameter
//            val offset = symbolTable.symbolTable.values.sumOf { if (it !is ParamAST) it.size else 0 }
//            return offset + offsetCount
//        }
        return 0
    }
    // Keeps checking the parent symbol table until the identifier is found
    if (symbolTable.parent != null) {
        return findParamInFuncOffset(symbolTable.parent!!, ident, symbolTable.symbolTable.size > 0,
            symbolTable.startingOffset)
    }
    return 0
}

/**
 * Calls helper function findParamInFuncOffset to compute offset
 *
 * @param symbolTable Symbol Table of the current scope
 * @param ident Identifier of the potential parameter
 * @return The offset of the parameter on the stack
 */
fun checkParamOffset(symbolTable: SymbolTable, ident: String): Int {
    return findParamInFuncOffset(symbolTable, ident, false, 0)
}