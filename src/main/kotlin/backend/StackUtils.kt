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

const val SIZE_OF_POINTER = 4
private const val MAX_STACK_OFFSET = 1024

fun calculateStackOffset(symbolTable : SymbolTable) : Int {
    var offset = 0
    for (astNode in symbolTable.symbolTable.values) {
        if (astNode is DeclareAST) {
            offset += astNode.size() // Potential optimisation to store offset in symbol table
        }
    }
    symbolTable.currOffset = offset
    return offset
}

fun allocateStack (symbolTable: SymbolTable, instructions: MutableList<Instruction>) : Int {
    val stackOffset = calculateStackOffset(symbolTable)
    symbolTable.totalDeclaredSize = stackOffset
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
        while (stackOffsetLeft > MAX_STACK_OFFSET) {
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
fun findIdentOffset(symbolTable: SymbolTable, ident: String, accOffset: Int = 0): Int {
    val totalOffset = accOffset + symbolTable.symbolTable.values.sumOf { it.size() }
    val returnPointerSize = 4
    var offsetCount = 0
    for ((key, node) in symbolTable.symbolTable) {
        if (key == ident && node is ParamAST) {
            return accOffset + symbolTable.totalDeclaredSize + offsetCount + returnPointerSize
        }
        offsetCount += node.size()
        if (key == ident && symbolTable.currOffset <= totalOffset - offsetCount) {
            return totalOffset - offsetCount
        }
    }
    if (symbolTable.parent != null) {
        /** Searches parent symbol table when not found in current scope.
         * Includes addition of totalOffset size of current scope */
        return findIdentOffset(symbolTable.parent!!, ident, totalOffset)
    }
    return totalOffset
}

fun checkFuncOffset(symbolTable: SymbolTable): Int{
    if (symbolTable is FuncSymbolTable) {
        return symbolTable.totalDeclaredSize
    }
    if (symbolTable.parent != null) {
        val offset = symbolTable.symbolTable.values.sumOf { it.size() }
        return checkFuncOffset(symbolTable.parent!!) + offset
    }
    return -1
}