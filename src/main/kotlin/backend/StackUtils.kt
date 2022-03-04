package backend

import backend.addressingmodes.ImmediateIntOperand
import backend.enums.Register
import backend.instruction.ArithmeticInstrType
import backend.instruction.ArithmeticInstruction
import backend.instruction.Instruction
import frontend.FuncSymbolTable
import frontend.SymbolTable
import frontend.ast.statement.DeclareAST

const val SIZE_OF_POINTER = 4
private const val MAX_STACK_OFFSET = 1024

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

fun allocateStack (symbolTable: SymbolTable, instructions: MutableList<Instruction>) : Int {
    val stackOffset = calculateStackOffset(symbolTable)
    //symbolTable.totalDeclaredSize = stackOffset
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

        if (addOrSubtract == ArithmeticInstrType.ADD) {
            ProgramState.stackPointer -= stackOffset
        } else if (addOrSubtract == ArithmeticInstrType.SUB) {
            ProgramState.stackPointer += stackOffset
        }
    }
}

/**
 * Finds the offset in stack for identifier
 * @param symbolTable The symbolTable of the current scope
 * @param ident The name of the variable
 * @return The offset in the stack for the variable
 */
fun findIdentOffset(symbolTable: SymbolTable, ident: String): Int {
    if (symbolTable.getStackPos(ident) != null) {
        return ProgramState.stackPointer - symbolTable.getStackPos(ident)!!
    }
    return findIdentOffset(symbolTable, ident)
}

fun checkFuncOffset(symbolTable: SymbolTable): Int{
    if (symbolTable is FuncSymbolTable) {
        return ProgramState.stackPointer - symbolTable.funcStackPos
    }
    return checkFuncOffset(symbolTable.parent!!)
}