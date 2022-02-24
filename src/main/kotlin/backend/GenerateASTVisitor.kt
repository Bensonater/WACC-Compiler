package backend

import backend.addressingmodes.ImmediateInt
import backend.enums.Register
import backend.addressingmodes.ImmediateIntOperand
import backend.instruction.*
import frontend.SymbolTable
import frontend.ast.*
import frontend.ast.literal.*
import frontend.ast.statement.*
import java.util.stream.Collectors

class GenerateASTVisitor (val programState: ProgramState) {

    private val MAX_STACK_OFFSET = 1024

    fun visit(ast: ASTNode) : List<Instruction>? {
        return ast.accept(this)
    }

    private fun getStackOffset(symbolTable : SymbolTable) : Int {
        var offset = 0
        for (astNode in symbolTable.symbolTable.values) {
            if (astNode is DeclareAST) {
                offset += astNode.type.size
            }
        }
        return offset
    }

    private fun allocateStack (symbolTable: SymbolTable, instructions: MutableList<Instruction>) : Int {
        val stackOffset = getStackOffset(symbolTable)
//            symbolTable.startingOffset = stackOffset
        moveStackPointer(ArithmeticInstrType.SUB, stackOffset, instructions)
        return stackOffset
    }

    private fun deallocateStack (stackOffset: Int, instructions: MutableList<Instruction>) {
        moveStackPointer(ArithmeticInstrType.ADD, stackOffset, instructions)
    }

    private fun moveStackPointer (addOrSubtract: ArithmeticInstrType, stackOffset: Int,
                                  instructions: MutableList<Instruction>) {
        if (stackOffset > 0) {
            var stackOffsetLeft = stackOffset
            while (stackOffsetLeft > MAX_STACK_OFFSET) {
                instructions.add(ArithmeticInstruction(addOrSubtract, Register.SP, Register.SP,
                    ImmediateIntOperand(MAX_STACK_OFFSET)))
                stackOffsetLeft -= MAX_STACK_OFFSET
            }
            instructions.add(ArithmeticInstruction(addOrSubtract, Register.SP, Register.SP,
                ImmediateIntOperand(stackOffsetLeft)))
        }
    }

    fun visitProgramAST(ast: ProgramAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()

        instructions.add(DirectiveInstruction("text"))
        instructions.add(DirectiveInstruction("global main"))

        val functionsInstructions = ast.funcList.stream().map { GenerateASTVisitor(programState).visit(it)}
            .collect(Collectors.toList())

        for (i in functionsInstructions) {
            instructions.addAll(i!!)
        }

        instructions.add (GeneralLabel("main"))

        instructions.add(PushInstruction(Register.LR))
        val stackOffset = allocateStack (ast.symbolTable, instructions)
        for (stat in ast.stats) {
            instructions.addAll(visit(stat)!!)
        }
        deallocateStack(stackOffset, instructions)


        instructions.add(LoadInstruction(ImmediateInt(0), Register.R0))
        instructions.add(EndInstruction())
        instructions.add(DirectiveInstruction("ltorg"))

        val data = ProgramState.dataDirective.translate()
//        val cLib = ProgramState.cLib.translate()

        return data + instructions // + cLib
    }

    fun visitFuncAST(ast: FuncAST): List<Instruction> {
        return mutableListOf()
    }


    /**
     * No code generation is required to translate ParamAST.
     */
    fun visitParamAST(ast: ParamAST): List<Instruction> {
        return emptyList()
    }

    fun visitBinOpExprAST(ast: BinOpExprAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitUnOpExprAST(ast: UnOpExprAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitIdentAST(ast: IdentAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitArrayElemAST(ast: ArrayElemAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitPairElemAST(ast: PairElemAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitNewPairAST(ast: NewPairAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitAssignAST(ast: AssignAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitBeginAST(ast: BeginAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitCallAST(ast: CallAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitDeclareAST(ast: DeclareAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitIfAST(ast: IfAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitReadAST(ast: ReadAST): List<Instruction> {
        return mutableListOf()
    }

    /**
     * No code generation is required to translate SkipAST.
     */
    fun visitSkipAST(ast: SkipAST): List<Instruction> {
        return emptyList()
    }

    /**
     * Translates multiple statements between BEGIN and END commands.
     */
    fun visitStatMultiAST(ast: StatMultiAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        ast.stats.forEach{ instructions.addAll(visit(it)!!)}
        return instructions
    }

    fun visitStatSimpleAST(ast: StatSimpleAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitWhileAST(ast: WhileAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitArrayLiterAST(ast: ArrayLiterAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitBoolLiterAST(ast: BoolLiterAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitCharLiterAST(ast: CharLiterAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitIntLiterAST(ast: IntLiterAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitNullPairLiterAST(ast: NullPairLiterAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitStrLiterAST(ast: StrLiterAST): List<Instruction> {
        return mutableListOf()
    }
}