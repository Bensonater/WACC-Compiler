package backend

import backend.addressingmodes.ImmediateIntOperand
import backend.enums.Register
import backend.instruction.ArithmeticInstrType
import backend.instruction.ArithmeticInstruction
import backend.instruction.Instruction
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
//            symbolTable.startingOffset = stackOffset
    moveStackPointer(ArithmeticInstrType.SUB, stackOffset, instructions)
    return stackOffset
}

fun deallocateStack (stackOffset: Int, instructions: MutableList<Instruction>) {
    moveStackPointer(ArithmeticInstrType.ADD, stackOffset, instructions)
}

fun moveStackPointer (addOrSubtract: ArithmeticInstrType, stackOffset: Int,
                              instructions: MutableList<Instruction>) {
    if (stackOffset > 0) {
        var stackOffsetLeft = stackOffset
        while (stackOffsetLeft > MAX_STACK_OFFSET) { // Why?
            instructions.add(
                ArithmeticInstruction(addOrSubtract, Register.SP, Register.SP,
                ImmediateIntOperand(MAX_STACK_OFFSET)
                )
            )
            stackOffsetLeft -= MAX_STACK_OFFSET
        }
        instructions.add(
            ArithmeticInstruction(addOrSubtract, Register.SP, Register.SP,
            ImmediateIntOperand(stackOffsetLeft)
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