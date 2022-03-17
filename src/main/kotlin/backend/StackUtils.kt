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
import language

const val SIZE_OF_POINTER = 4
private const val MAX_STACK_OFFSET = 1024

/**
 * Calculate the size needed for new declare variables to allocate on stack in this scope
 * @param symbolTable The symbol table of this scope
 * @return The size in bytes of the total declared variables
 */
fun calculateStackOffset(symbolTable : SymbolTable) : Int {
    var offset = 0
    for (astNode in symbolTable.symbolTable.values) {
        if (astNode is DeclareAST) {
            offset += astNode.size()
        }
    }
    symbolTable.currOffset = offset
    return offset
}

/**
 * Allocate the required size on the stack and add to instructions list
 * @param symbolTable The symbol table of the current scope
 * @param instructions The instructions list to add instructions to
 * @return The size of the stack allocated in bytes
 */
fun allocateStack (symbolTable: SymbolTable, instructions: MutableList<Instruction>) : Int {
    val stackOffset = calculateStackOffset(symbolTable)
    symbolTable.totalDeclaredSize = stackOffset
    moveStackPointer(ArithmeticInstrType.SUB, stackOffset, instructions)
    return stackOffset
}

/**
 * Deallocate the required size on the stack and add to instructions list
 * @param stackOffset The size required to deallocate on the stack
 * @param instructions The instructions list to add instructions to
 * @return The size of the stack deallocated in bytes
 */
fun deallocateStack (stackOffset: Int, instructions: MutableList<Instruction>) {
    moveStackPointer(ArithmeticInstrType.ADD, stackOffset, instructions)
}

/**
 * Helper function to either move the stack pointer up or down
 * @param addOrSubtract Indicate to either increment or decrement stack pointer
 * @param stackOffset The size required to offset on the stack
 * @param instructions The instructions list to add instructions to
 */
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
 */
fun findIdentOffset(symbolTable: SymbolTable, ident: String, accOffset: Int = 0): Int {
    val totalOffset = accOffset + symbolTable.symbolTable.values.sumOf { it.size() }
    val returnPointerSize = if (language == Language.X86_64) 16 else 4
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

/**
 * Helper function to check how much the stack pointer should offset when
 * return statement is reached in a function.
 * @param symbolTable The symbol table of the current scope
 * @return The offset required to move the stack pointer at return statement
 */
fun checkFuncOffset(symbolTable: SymbolTable): Int{
    if (symbolTable is FuncSymbolTable) {
        return symbolTable.totalDeclaredSize
    }
    val offset = symbolTable.symbolTable.values.sumOf { it.size() }
    return checkFuncOffset(symbolTable.parent!!) + offset
}