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
        }
        when (ast.binOp) {
            IntBinOp.PLUS -> {
                if (accumUsed) {
                    instructions.add(PopInstruction(Register.R12))
                    instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, reg1, reg2, RegisterOperand(reg1)))
                } else {
                    instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, reg1, reg1, RegisterOperand(reg2)))
                }
                instructions.add(BranchInstruction(Condition.VS, RuntimeErrors.throwOverflowErrorLabel, true))
                ProgramState.runtimeErrors.addOverflowError()
            }
            IntBinOp.MINUS -> {
                if (accumUsed) {
                    instructions.add(PopInstruction(Register.R12))
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
                    instructions.add(PopInstruction(Register.R12))
                    instructions.add(MultiplyInstruction(Condition.AL, reg1, reg2, reg2, reg1))
                } else {
                    instructions.add(MultiplyInstruction(Condition.AL, reg1, reg2, reg1, reg2))
                }
                instructions.add(CompareInstruction(reg2, RegisterOperandWithShift(reg1, ShiftType.ASR, shiftAmount)))
                instructions.add(BranchInstruction(Condition.NE, RuntimeErrors.throwOverflowErrorLabel, true))
                ProgramState.runtimeErrors.addOverflowError()
            }
            IntBinOp.DIV -> {
                if (accumUsed) {
                    instructions.add(PopInstruction(Register.R12))
                    instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg2)))
                    instructions.add(MoveInstruction(Condition.AL, Register.R1, RegisterOperand(reg1)))
                } else {
                    instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg1)))
                    instructions.add(MoveInstruction(Condition.AL, Register.R1, RegisterOperand(reg2)))
                }
                instructions.add(BranchInstruction(Condition.AL, RuntimeErrors.divideZeroCheckLabel, true))
                ProgramState.runtimeErrors.addDivideByZeroCheck()
                instructions.add(BranchInstruction(Condition.AL, GeneralLabel("__aeabi_idiv"), true))
                instructions.add(MoveInstruction(Condition.AL, reg1, RegisterOperand(Register.R0)))
            }
            IntBinOp.MOD -> {
                if (accumUsed) {
                    instructions.add(PopInstruction(Register.R12))
                    instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg2)))
                    instructions.add(MoveInstruction(Condition.AL, Register.R1, RegisterOperand(reg1)))
                } else {
                    instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg1)))
                    instructions.add(MoveInstruction(Condition.AL, Register.R1, RegisterOperand(reg2)))
                }
                instructions.add(BranchInstruction(Condition.AL, RuntimeErrors.divideZeroCheckLabel, true))
                ProgramState.runtimeErrors.addDivideByZeroCheck()
                instructions.add(BranchInstruction(Condition.AL, GeneralLabel("__aeabi_idivmod"), true))
                instructions.add(MoveInstruction(Condition.AL, reg1, RegisterOperand(Register.R1)))
            }
            CmpBinOp.EQ -> {
                if (accumUsed) {
                    instructions.add(PopInstruction(Register.R12))
                    instructions.add(CompareInstruction(reg2, RegisterOperand(reg1)))
                } else {
                    instructions.add(CompareInstruction(reg1, RegisterOperand(reg2)))
                }
                instructions.add(MoveInstruction(Condition.EQ, reg1, ImmediateBoolOperand(true)))
                instructions.add(MoveInstruction(Condition.NE, reg1, ImmediateBoolOperand(false)))
            }
            CmpBinOp.GT -> {
                if (accumUsed) {
                    instructions.add(PopInstruction(Register.R12))
                    instructions.add(CompareInstruction(reg2, RegisterOperand(reg1)))
                } else {
                    instructions.add(CompareInstruction(reg1, RegisterOperand(reg2)))
                }
                instructions.add(MoveInstruction(Condition.GT, reg1, ImmediateBoolOperand(true)))
                instructions.add(MoveInstruction(Condition.LE, reg1, ImmediateBoolOperand(false)))
            }
            CmpBinOp.LT -> {
                if (accumUsed) {
                    instructions.add(PopInstruction(Register.R12))
                    instructions.add(CompareInstruction(reg2, RegisterOperand(reg1)))
                } else {
                    instructions.add(CompareInstruction(reg1, RegisterOperand(reg2)))
                }
                instructions.add(MoveInstruction(Condition.LT, reg1, ImmediateBoolOperand(true)))
                instructions.add(MoveInstruction(Condition.GE, reg1, ImmediateBoolOperand(false)))
            }
            CmpBinOp.GTE -> {
                if (accumUsed) {
                    instructions.add(PopInstruction(Register.R12))
                    instructions.add(CompareInstruction(reg2, RegisterOperand(reg1)))
                } else {
                    instructions.add(CompareInstruction(reg1, RegisterOperand(reg2)))
                }
                instructions.add(MoveInstruction(Condition.GE, reg1, ImmediateBoolOperand(true)))
                instructions.add(MoveInstruction(Condition.LT, reg1, ImmediateBoolOperand(false)))
            }
            CmpBinOp.LTE -> {
                if (accumUsed) {
                    instructions.add(PopInstruction(Register.R12))
                    instructions.add(CompareInstruction(reg2, RegisterOperand(reg1)))
                } else {
                    instructions.add(CompareInstruction(reg1, RegisterOperand(reg2)))
                }
                instructions.add(MoveInstruction(Condition.LE, reg1, ImmediateBoolOperand(true)))
                instructions.add(MoveInstruction(Condition.GT, reg1, ImmediateBoolOperand(false)))
            }
            CmpBinOp.NEQ -> {
                if (accumUsed) {
                    instructions.add(PopInstruction(Register.R12))
                    instructions.add(CompareInstruction(reg2, RegisterOperand(reg1)))
                } else {
                    instructions.add(CompareInstruction(reg1, RegisterOperand(reg2)))
                }
                instructions.add(MoveInstruction(Condition.NE, reg1, ImmediateBoolOperand(true)))
                instructions.add(MoveInstruction(Condition.EQ, reg1, ImmediateBoolOperand(false)))
            }
            BoolBinOp.AND -> {
                if (accumUsed) {
                    instructions.add(PopInstruction(Register.R12))
                    instructions.add(LogicInstruction(LogicOperation.AND, reg1, reg1, RegisterOperand(reg2)))
                } else {
                    instructions.add(LogicInstruction(LogicOperation.AND, reg1, reg1, RegisterOperand(reg2)))
                }
            }
            BoolBinOp.OR -> {
                if (accumUsed) {
                    instructions.add(PopInstruction(Register.R12))
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
        var reg = programState.getFreeCalleeReg()
        val instructions = mutableListOf<Instruction>()
        if (reg == Register.NONE) {
            reg = Register.R11
            instructions.add(PushInstruction(reg))
        }
        instructions.add(MoveInstruction(Condition.AL, reg, ImmediateBoolOperand(ast.value)))
        return instructions
    }

    fun visitCharLiterAST(ast: CharLiterAST): List<Instruction> {
        var reg = programState.getFreeCalleeReg()
        val instructions = mutableListOf<Instruction>()
        if (reg == Register.NONE) {
            reg = Register.R11
            instructions.add(PushInstruction(reg))
        }
        instructions.add(MoveInstruction(Condition.AL, reg, ImmediateCharOperand(ast.value)))
        return instructions
    }

    fun visitIntLiterAST(ast: IntLiterAST): List<Instruction> {
        var reg = programState.getFreeCalleeReg()
        val instructions = mutableListOf<Instruction>()
        if (reg == Register.NONE) {
            reg = Register.R11
            instructions.add(PushInstruction(reg))
        }
        instructions.add(LoadInstruction(Condition.AL, ImmediateInt(ast.value), reg))
        return instructions
    }

    fun visitNullPairLiterAST(ast: NullPairLiterAST): List<Instruction> {
        var reg = programState.getFreeCalleeReg()
        val instructions = mutableListOf<Instruction>()
        if (reg == Register.NONE) {
            reg = Register.R11
            instructions.add(PushInstruction(reg))
        }
        instructions.add(LoadInstruction(Condition.AL, ImmediateInt(0), reg))
        return instructions
    }

    fun visitStrLiterAST(ast: StrLiterAST): List<Instruction> {
        var reg = programState.getFreeCalleeReg()
        val instructions = mutableListOf<Instruction>()
        if (reg == Register.NONE) {
            reg = Register.R11
            instructions.add(PushInstruction(reg))
        }
        val strLabel = ProgramState.dataDirective.addStringLabel(ast.value)
        instructions.add(LoadInstruction(Condition.AL, ImmediateLabel(strLabel), reg))
        return instructions
    }
}