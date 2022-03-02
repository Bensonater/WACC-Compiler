package backend

import backend.addressingmodes.*
import backend.enums.Register
import backend.enums.Condition
import backend.enums.Memory
import backend.global.RuntimeErrors
import backend.instruction.*
import frontend.ast.*
import frontend.ast.literal.*
import frontend.ast.statement.*
import frontend.ast.type.BaseType
import frontend.ast.type.BaseTypeAST
import frontend.ast.type.PairTypeAST
import java.util.stream.Collectors

class GenerateASTVisitor (val programState: ProgramState) {

    fun visit(ast: ASTNode) : List<Instruction>? {
        return ast.accept(this)
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


        instructions.add(LoadInstruction(Condition.AL, ImmediateInt(0), Register.R0))
        instructions.add(EndInstruction())
        instructions.add(DirectiveInstruction("ltorg"))

        val data = ProgramState.dataDirective.translate()
        val library = ProgramState.library.translate()

        return data + instructions + library
    }

    fun visitFuncAST(ast: FuncAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        instructions.add(FunctionLabel(ast.ident.name))
        instructions.add(PushInstruction(Register.LR))
        val offset = calculateStackOffset(ast.symbolTable)
        ast.symbolTable.startingOffset = offset
        if (offset > 0) {
            instructions.add(ArithmeticInstruction(ArithmeticInstrType.SUB,
                Register.SP, Register.SP, ImmediateIntOperand(offset)))
        }

        for (stat in ast.stats) {
            instructions.addAll(visit(stat)!!)
        }

//        if (ast.stats.last() is IfAST) )
        TODO("Check the last stat is exit or return")
        if (offset > 0) {
            instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, Register.SP, Register.SP, ImmediateIntOperand(offset)))
        }
        instructions.add(PopInstruction(Register.PC))
        instructions.add(DirectiveInstruction("ltorg"))
        programState.freeAllCalleeRegs()
        return instructions
    }


    /**
     * No code generation is required to translate ParamAST.
     */
    fun visitParamAST(ast: ParamAST): List<Instruction> {
        return emptyList()
    }

    fun visitBinOpExprAST(ast: BinOpExprAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()

        instructions.addAll(visit(ast.expr1)!!)
        var reg1 = programState.recentlyUsedCalleeReg()
        instructions.addAll(visit(ast.expr2)!!)
        var reg2 = programState.recentlyUsedCalleeReg()

        var accumUsed = false
        if (reg1 == Register.NONE || reg1 == Register.R11) {
            accumUsed = true
            reg1 = Register.R11
            reg2 = Register.R12
            instructions.add(PopInstruction(Register.R12))
        }
        when (ast.binOp) {
            IntBinOp.PLUS -> {
                if (accumUsed) {
                    instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, reg1, reg2, RegisterOperand(reg1)))
                } else {
                    instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, reg1, reg1, RegisterOperand(reg2)))
                }
                instructions.add(BranchInstruction(Condition.VS, RuntimeErrors.throwOverflowErrorLabel, true))
                ProgramState.runtimeErrors.addOverflowError()
            }
            IntBinOp.MINUS -> {
                if (accumUsed) {
                    instructions.add(ArithmeticInstruction(ArithmeticInstrType.SUB, reg1, reg2, RegisterOperand(reg1)))
                } else {
                    instructions.add(ArithmeticInstruction(ArithmeticInstrType.SUB, reg1, reg1, RegisterOperand(reg2)))
                }
                instructions.add(BranchInstruction(Condition.VS, RuntimeErrors.throwOverflowErrorLabel, true))
                ProgramState.runtimeErrors.addOverflowError()
            }
            IntBinOp.MULT -> {
                val shiftAmount = 31
                if (accumUsed) {
                    instructions.add(MultiplyInstruction(Condition.AL, reg1, reg2, reg2, reg1))
                } else {
                    instructions.add(MultiplyInstruction(Condition.AL, reg1, reg2, reg1, reg2))
                }
                instructions.add(CompareInstruction(reg2, RegisterOperandWithShift(reg1, ShiftType.ASR, shiftAmount)))
                instructions.add(BranchInstruction(Condition.NE, RuntimeErrors.throwOverflowErrorLabel, true))
                ProgramState.runtimeErrors.addOverflowError()
            }
            IntBinOp.DIV, IntBinOp.MOD -> {
                if (accumUsed) {
                    instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg2)))
                    instructions.add(MoveInstruction(Condition.AL, Register.R1, RegisterOperand(reg1)))
                } else {
                    instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg1)))
                    instructions.add(MoveInstruction(Condition.AL, Register.R1, RegisterOperand(reg2)))
                }
                instructions.add(BranchInstruction(Condition.AL, RuntimeErrors.divideZeroCheckLabel, true))
                ProgramState.runtimeErrors.addDivideByZeroCheck()
                when (ast.binOp) {
                    IntBinOp.DIV -> {
                        instructions.add(BranchInstruction(Condition.AL, GeneralLabel("__aeabi_idiv"), true))
                        instructions.add(MoveInstruction(Condition.AL, reg1, RegisterOperand(Register.R0)))
                    }
                    IntBinOp.MOD -> {
                        instructions.add(BranchInstruction(Condition.AL, GeneralLabel("__aeabi_idivmod"), true))
                        instructions.add(MoveInstruction(Condition.AL, reg1, RegisterOperand(Register.R1)))
                    }
                }
            }
            is CmpBinOp -> {
                if (accumUsed) {
                    instructions.add(CompareInstruction(reg2, RegisterOperand(reg1)))
                } else {
                    instructions.add(CompareInstruction(reg1, RegisterOperand(reg2)))
                }

                instructions.add(MoveInstruction(ast.binOp.cond, reg1, ImmediateBoolOperand(true)))
                instructions.add(MoveInstruction(ast.binOp.opposite, reg1, ImmediateBoolOperand(false)))
            }
            BoolBinOp.AND -> {
                if (accumUsed) {
                    instructions.add(LogicInstruction(LogicOperation.AND, reg1, reg1, RegisterOperand(reg2)))
                } else {
                    instructions.add(LogicInstruction(LogicOperation.AND, reg1, reg1, RegisterOperand(reg2)))
                }
            }
            BoolBinOp.OR -> {
                if (accumUsed) {
                    instructions.add(LogicInstruction(LogicOperation.OR, reg1, reg2, RegisterOperand(reg1)))
                } else {
                    instructions.add(LogicInstruction(LogicOperation.OR, reg1, reg1, RegisterOperand(reg2)))
                }
            }
        }
        if (!accumUsed) {
            programState.freeCalleeReg()
        }
        return instructions
    }

    fun visitUnOpExprAST(ast: UnOpExprAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        instructions.addAll(visit(ast.expr)!!)
        val reg = programState.recentlyUsedCalleeReg()
        when (ast.unOp) {
            UnOp.NOT -> {
                instructions.add(LogicInstruction(LogicOperation.EOR, reg, reg, ImmediateIntOperand(1)))
            }
            UnOp.MINUS -> {
                instructions.add(ArithmeticInstruction(ArithmeticInstrType.RSB, reg, reg, ImmediateIntOperand(0)))
                instructions.add(BranchInstruction(Condition.VS, RuntimeErrors.throwOverflowErrorLabel, true))
                ProgramState.runtimeErrors.addOverflowError()
            }
            UnOp.LEN -> {
                instructions.add(LoadInstruction(Condition.AL, RegisterMode(Register.SP), reg))
                instructions.add(LoadInstruction(Condition.AL, RegisterMode(reg), reg))
            }
        }
        return instructions
    }

    fun visitIdentAST(ast: IdentAST): List<Instruction> {
        val offset = findIdentOffset(ast.symbolTable, ast.name) +
                checkParamOffset(ast.symbolTable, ast.name) + ast.symbolTable.callOffset
        val typeAST = ast.getType(ast.symbolTable)
        val isBoolOrChar = typeAST is BaseTypeAST && (typeAST.type == BaseType.BOOL || typeAST.type == BaseType.CHAR)
        val memoryType = if (isBoolOrChar) Memory.SB else null
        return listOf(LoadInstruction(Condition.AL, RegisterModeWithOffset(Register.SP, offset),
            programState.getFreeCalleeReg(), memoryType))
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
        val instructions = mutableListOf<Instruction>()
        val stackOffset = allocateStack (ast.symbolTable, instructions)
        for (stat in ast.stats) {
            instructions.addAll(visit(stat)!!)
        }
        deallocateStack(stackOffset, instructions)
        return instructions
    }

    fun visitCallAST(ast: CallAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitDeclareAST(ast: DeclareAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        instructions.addAll(visit(ast.assignRhs)!!)

        if (ast.assignRhs is StrLiterAST) {
            ast.label = ProgramState.dataDirective.toStringLabel(ast.assignRhs.value)
        }
        decreaseOffset(ast.symbolTable, ast.ident, ast.assignRhs.getType(ast.symbolTable)!!)
        var memory: Memory? = null
        when (ast.type) {
            is BaseTypeAST -> {
                if ((ast.type.type == BaseType.BOOL) || (ast.type.type == BaseType.CHAR)) {
                    memory = Memory.B
                }
            }
            is PairTypeAST -> {
                if (ast.assignRhs !is NewPairAST && ast.assignRhs !is ArrayElemAST && ast.assignRhs !is IdentAST &&
                    ast.assignRhs !is NullPairLiterAST && ast.assignRhs !is CallAST && ast.assignRhs !is PairElemAST) {
                    instructions.add(LoadInstruction(Condition.AL,
                        RegisterMode(programState.recentlyUsedCalleeReg()), programState.recentlyUsedCalleeReg()))
                }
            }
        }
        when (ast.assignRhs) {
            is PairElemAST -> {
                instructions.add(LoadInstruction(Condition.AL, RegisterMode(programState.recentlyUsedCalleeReg()),
                    programState.recentlyUsedCalleeReg(), memory))
            }
            is ArrayElemAST -> {
                instructions.add(LoadInstruction(Condition.AL, RegisterMode(programState.recentlyUsedCalleeReg()),
                    programState.recentlyUsedCalleeReg()))
            }
        }
        instructions.add(StoreInstruction(RegisterModeWithOffset(Register.SP, ast.symbolTable.currOffset),
            programState.recentlyUsedCalleeReg(), memory))
        programState.freeCalleeReg()

        return instructions
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


    /**
     * Translate an array literal AST, e.g. [19, 21, 3, a, 7] where a = 30
     */
    fun visitArrayLiterAST(ast: ArrayLiterAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        val elemSize = ast.getType(ast.symbolTable).size

        val sizeOfInt = 4
        instructions.add(LoadInstruction(Condition.AL, ImmediateInt(elemSize * ast.vals.size + sizeOfInt), Register.R0))
        instructions.add(BranchInstruction(Condition.AL, GeneralLabel("malloc"), true))
        val stackReg = programState.getFreeCalleeReg()
        instructions.add(MoveInstruction(Condition.AL, stackReg, RegisterOperand(Register.R0)))

        var memType: Memory? = null
        for ((index, expr) in ast.vals.withIndex()) {
//            if (expr is IdentAST) {
//                expr.symbolTable = ast.symbolTable
//            }
            instructions.addAll(visit(expr)!!)
            if ((expr is CharLiterAST) || (expr is BoolLiterAST)) {
                memType = Memory.B
            }
            instructions.add(StoreInstruction(RegisterModeWithOffset(stackReg, sizeOfInt + (index * elemSize)), programState.recentlyUsedCalleeReg(), memType))
            programState.freeCalleeReg()
        }

        instructions.add(LoadInstruction(Condition.AL, ImmediateInt(ast.vals.size), programState.getFreeCalleeReg()))
        instructions.add(StoreInstruction(RegisterMode(stackReg), programState.recentlyUsedCalleeReg()))
        programState.freeCalleeReg()
        return instructions
    }

    /**
     * Translate a boolean literal AST.
     */
    fun visitBoolLiterAST(ast: BoolLiterAST): List<Instruction> {
        return visitLiterHelper(ImmediateBoolOperand(ast.value), false)
    }

    /**
     * Translate a character literal AST.
     */
    fun visitCharLiterAST(ast: CharLiterAST): List<Instruction> {
        return visitLiterHelper(ImmediateCharOperand(ast.value), false)
    }

    /**
     * Translate an integer literal AST.
     */
    fun visitIntLiterAST(ast: IntLiterAST): List<Instruction> {
        return visitLiterHelper(ImmediateInt(ast.value), true)
    }

    /**
     * Translate a null pair literal AST.
     */
    fun visitNullPairLiterAST(ast: NullPairLiterAST): List<Instruction> {
        return visitLiterHelper(ImmediateInt(0), true)
    }

    /**
     * Translate a string literal AST.
     */
    fun visitStrLiterAST(ast: StrLiterAST): List<Instruction> {
        val strLabel = ProgramState.dataDirective.addStringLabel(ast.value)
        return visitLiterHelper(ImmediateLabel(strLabel), true)
    }

    private fun visitLiterHelper(param : AddressingMode, load: Boolean) : List<Instruction> {
        var reg = programState.getFreeCalleeReg()
        val instructions = mutableListOf<Instruction>()
        // Check if the registers are all full, then use accumulator
        if (reg == Register.NONE) {
            reg = Register.R11
            instructions.add(PushInstruction(reg))
        }
        if (load) {
            instructions.add(LoadInstruction(Condition.AL, param, reg))
        } else {
            instructions.add(MoveInstruction(Condition.AL, reg, param))
        }
        return instructions
    }
}