package backend

import backend.addressingmodes.*
import backend.enums.Register
import backend.enums.Condition
import backend.enums.Memory
import backend.global.CallFunc
import backend.global.Funcs
import backend.global.RuntimeErrors
import backend.instruction.*
import frontend.ast.*
import frontend.ast.literal.*
import frontend.ast.statement.*
import frontend.ast.type.*
import java.util.stream.Collectors

class GenerateASTVisitor (val programState: ProgramState) {

    fun visit(ast: ASTNode) : List<Instruction> {
        return ast.accept(this)!!
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
            instructions.addAll(visit(stat))
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
            instructions.addAll(visit(stat))
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

        instructions.addAll(visit(ast.expr1))
        var reg1 = programState.recentlyUsedCalleeReg()
        instructions.addAll(visit(ast.expr2))
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
        instructions.addAll(visit(ast.expr))
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

    fun visitPairElemAST(ast: PairElemAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        instructions.addAll(visit(ast.expr))
        val reg = programState.recentlyUsedCalleeReg()
        instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg)))
        instructions.add(BranchInstruction(Condition.AL, RuntimeErrors.nullReferenceLabel, true))
        ProgramState.runtimeErrors.addNullReferenceCheck()
        if (ast.index == PairIndex.FST) {
            instructions.add(LoadInstruction(Condition.AL, RegisterMode(reg), reg))
        } else {
            instructions.add(LoadInstruction(Condition.AL, RegisterModeWithOffset(reg, 4), reg))
        }
        return instructions
    }

    fun visitNewPairAST(ast: NewPairAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        var memoryType: Memory? = null

        // Malloc space for two pointers to the first and second elements
        instructions.add(LoadInstruction(Condition.AL, ImmediateInt(8), Register.R0))
        instructions.add(BranchInstruction(Condition.AL, GeneralLabel(Funcs.MALLOC.toString()), true))
        val stackReg = programState.getFreeCalleeReg()
        instructions.add(MoveInstruction(Condition.AL, stackReg, RegisterOperand(Register.R0)))

        // Malloc first element
        instructions.addAll(visit(ast.fst))
        val fstType = ast.fst.getType(ast.symbolTable)!!
        instructions.add(LoadInstruction(Condition.AL, ImmediateInt(fstType.size), Register.R0))
        instructions.add(BranchInstruction(Condition.AL, GeneralLabel(Funcs.MALLOC.toString()), true))

        if (fstType is BaseTypeAST && (fstType.type == BaseType.BOOL || fstType.type == BaseType.CHAR)) {
            memoryType = Memory.B
        }
        instructions.add(StoreInstruction(RegisterMode(Register.R0), programState.recentlyUsedCalleeReg(), memoryType))
        programState.freeCalleeReg()
        instructions.add(StoreInstruction(RegisterMode(stackReg), Register.R0))

        // Malloc second element
        instructions.addAll(visit(ast.snd))
        val sndType = ast.snd.getType(ast.symbolTable)!!
        instructions.add(LoadInstruction(Condition.AL, ImmediateInt(sndType.size), Register.R0))
        instructions.add(BranchInstruction(Condition.AL, GeneralLabel(Funcs.MALLOC.toString()), true))
        if (sndType is BaseTypeAST && (sndType.type == BaseType.BOOL || sndType.type == BaseType.CHAR)) {
            memoryType = Memory.B
        }
        instructions.add(StoreInstruction(RegisterMode(Register.R0), programState.recentlyUsedCalleeReg(), memoryType))
        programState.freeCalleeReg()
        instructions.add(StoreInstruction(RegisterModeWithOffset(stackReg, 4), Register.R0))

        return instructions
    }

    fun visitAssignAST(ast: AssignAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()

        instructions.addAll(visit(ast.assignRhs))
        val calleeReg = programState.recentlyUsedCalleeReg()
        if (ast.assignRhs is StrLiterAST) {
            ast.label = ProgramState.dataDirective.toStringLabel(ast.assignRhs.value)
        }

        val rhsType = ast.assignRhs.getType(ast.symbolTable)
        var memtype: Memory? = null
        if (rhsType is BaseTypeAST && ((rhsType.type == BaseType.CHAR) || (rhsType.type == BaseType.BOOL))) {
            memtype = Memory.B
        }

        if (ast.assignRhs is PairElemAST) {
            instructions.add(LoadInstruction(Condition.AL, RegisterMode(calleeReg), calleeReg))
        }

        when (ast.assignLhs) {
            is IdentAST -> {
                val offset = findIdentOffset(ast.symbolTable, ast.assignLhs.name)
//                var (correctSTScope, offset) = ast.symbolTable.getSTWithIdentifier(ast.assignLhs.name, rhsType)
//                offset += checkParamOffset(ast.symbolTable, ast.assignLhs.name)
                instructions.add(StoreInstruction(RegisterModeWithOffset(Register.SP, offset), calleeReg, memtype))
            }
            is ArrayElemAST -> {
                instructions.addAll(visit(ast.assignLhs))
                instructions.add(StoreInstruction(RegisterMode(programState.recentlyUsedCalleeReg()), calleeReg, memtype))
                programState.freeCalleeReg()
            }
            is PairElemAST -> {
                instructions.addAll(visit(ast.assignLhs))
                instructions.add(StoreInstruction(RegisterMode(programState.recentlyUsedCalleeReg()), calleeReg, memtype))
                programState.freeCalleeReg()
            }
        }
        programState.freeCalleeReg()
        return instructions
    }

    fun visitBeginAST(ast: BeginAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        val stackOffset = allocateStack (ast.symbolTable, instructions)
        for (stat in ast.stats) {
            instructions.addAll(visit(stat))
        }
        deallocateStack(stackOffset, instructions)
        return instructions
    }

    fun visitCallAST(ast: CallAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        ast.args.forEach { instructions.addAll(visit(it)) }
        programState.freeCalleeReg()
        return instructions
    }

    fun visitDeclareAST(ast: DeclareAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        instructions.addAll(visit(ast.assignRhs))

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
        val instructions = mutableListOf<Instruction>()
        val elseLabel = programState.getNextLabel()
        val finalLabel = programState.getNextLabel()

        instructions.addAll(visit(ast.expr))
        instructions.add(CompareInstruction(programState.recentlyUsedCalleeReg(), ImmediateIntOperand(0)))
        instructions.add(BranchInstruction(Condition.EQ, elseLabel, false))
        programState.freeCalleeReg()
        var stackOffset = calculateStackOffset(ast.thenSymbolTable)
        if (stackOffset > 0) {
            instructions.add(ArithmeticInstruction(ArithmeticInstrType.SUB, Register.SP,Register.SP, ImmediateIntOperand(stackOffset)))
        }

        ast.thenStat.forEach{
            instructions.addAll(visit(it))
        }

        instructions.add(BranchInstruction(Condition.AL, finalLabel, false))
        instructions.add(elseLabel)
        stackOffset = calculateStackOffset(ast.elseSymbolTable)
        if (stackOffset > 0) {
            instructions.add(ArithmeticInstruction(ArithmeticInstrType.SUB, Register.SP,Register.SP, ImmediateIntOperand(stackOffset)))
        }

        ast.elseStat.forEach{
            instructions.addAll(visit(it))
        }

        instructions.add(finalLabel)

        return instructions
    }

    fun visitReadAST(ast: ReadAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        when (ast.assignLhs) {
            is IdentAST -> {
                instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, Register.R4, Register.SP, ImmediateIntOperand(
                    findIdentOffset(ast.symbolTable,ast.assignLhs.name)
                ))) }
            is ArrayElemAST -> {
                // Intentionally Left Blank
            }
            is PairElemAST -> {
                /** Translates the expression */
                instructions.addAll(visit(ast.assignLhs))
            }
        }
        instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(Register.R4)))

        /** Adds specific calls to read library functions */
        when ((ast.assignLhs as BaseTypeAST).type) {
            BaseType.INT -> {
                instructions.add(BranchInstruction(Condition.AL, GeneralLabel(CallFunc.READ_INT.toString()), true))
                ProgramState.library.addCode(CallFunc.READ_INT)
            }
            BaseType.CHAR -> {
                instructions.add(BranchInstruction(Condition.AL, GeneralLabel(CallFunc.READ_CHAR.toString()), true))
                ProgramState.library.addCode(CallFunc.READ_CHAR)
            }
        }
        return instructions
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
        ast.stats.forEach{ instructions.addAll(visit(it))}
        return instructions
    }

    fun visitStatSimpleAST(ast: StatSimpleAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        instructions.addAll(visit(ast.expr))

        val reg = programState.recentlyUsedCalleeReg()
        val exprType = ast.expr.getType(ast.symbolTable)!!

        if (ast.expr is ArrayElemAST) {
            var memType: Memory? = null
            if ((exprType is BaseTypeAST) && ((exprType.type == BaseType.BOOL) || (exprType.type == BaseType.CHAR))) {
                memType = Memory.SB
            }
            instructions.add(LoadInstruction(Condition.AL, RegisterMode(reg), reg, memType))
        }
        when (ast.command) {
            Command.EXIT -> {
                instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg)))
                instructions.add(BranchInstruction(Condition.AL, GeneralLabel("exit"), true))
                programState.freeAllCalleeRegs()
            }
            Command.PRINT, Command.PRINTLN -> {
                /** Adds specific code for printing.*/
                when (exprType) {
                    is BaseTypeAST -> {
                        instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg)))

                        when (exprType.type) {
                            BaseType.INT -> {
                                ProgramState.library.addCode(CallFunc.PRINT_INT)
                                instructions.add(BranchInstruction(Condition.AL, GeneralLabel(CallFunc.PRINT_INT.toString()), true))
                            }
                            BaseType.CHAR -> {
                                instructions.add(BranchInstruction(Condition.AL, GeneralLabel(Funcs.PUTCHAR.toString()), true))
                            }
                            BaseType.BOOL -> {
                                ProgramState.library.addCode(CallFunc.PRINT_BOOL)
                                instructions.add(BranchInstruction(Condition.AL, GeneralLabel(CallFunc.PRINT_BOOL.toString()), true))
                            }
                            BaseType.STRING -> {
                                ProgramState.library.addCode(CallFunc.PRINT_STRING)
                                instructions.add(BranchInstruction(Condition.AL, GeneralLabel(CallFunc.PRINT_STRING.toString()), true))
                            }
                        }
                    }
                    is ArrayTypeAST -> {
                        instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg)))
                        if (exprType.type is BaseTypeAST && (exprType.type.type == BaseType.CHAR)) {
                            instructions.add(BranchInstruction(Condition.AL, GeneralLabel(CallFunc.PRINT_STRING.toString()), true))
                            ProgramState.library.addCode(CallFunc.PRINT_STRING)
                        } else {
                            instructions.add(BranchInstruction(Condition.AL, GeneralLabel(CallFunc.PRINT_REFERENCE.toString()), true))
                            ProgramState.library.addCode(CallFunc.PRINT_REFERENCE)
                        }
                    }
                    is PairTypeAST, is ArbitraryTypeAST -> {
                        instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg)))
                        instructions.add(BranchInstruction(Condition.AL, GeneralLabel(CallFunc.PRINT_REFERENCE.toString()), true))
                        ProgramState.library.addCode(CallFunc.PRINT_REFERENCE)
                    }
                }
                if (ast.command == Command.PRINTLN) {
                    ProgramState.library.addCode(CallFunc.PRINT_LN)
                    instructions.add(BranchInstruction(Condition.AL, GeneralLabel(CallFunc.PRINT_LN.toString()), true))
                }
                programState.freeCalleeReg()
            }
            Command.FREE -> {
                instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(programState.recentlyUsedCalleeReg())))
                val methodName : CallFunc = if (exprType is ArrayTypeAST) {
                    CallFunc.FREE_ARRAY
                } else {
                    CallFunc.FREE_PAIR
                }

                instructions.add(BranchInstruction(Condition.AL, GeneralLabel(methodName.toString()), true))
                ProgramState.library.addCode(methodName)
                programState.freeCalleeReg()
            }
            Command.RETURN -> {
                instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg)))
                instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, Register.SP, Register.SP, ImmediateIntOperand(
                    checkFuncOffset(ast.symbolTable)
                )))
                instructions.add(PopInstruction(Register.PC))
                programState.freeAllCalleeRegs()
            }
        }
        return instructions
    }

    fun visitWhileAST(ast: WhileAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        val conditionLabel = programState.getNextLabel()
        val bodyLabel = programState.getNextLabel()
        instructions.add(BranchInstruction(Condition.AL, conditionLabel, false))

        instructions.add(bodyLabel)
        val stackOffset = calculateStackOffset(ast.bodySymbolTable)
        ast.bodySymbolTable.startingOffset = stackOffset
        if (stackOffset > 0) {
            instructions.add(ArithmeticInstruction(ArithmeticInstrType.SUB, Register.SP, Register.SP, ImmediateIntOperand(stackOffset)))
        }
        /** Translates all the statements within the while loop body */
        for (stat in ast.stats) {
            instructions.addAll(visit(stat))
        }
        if (stackOffset > 0) {
            instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, Register.SP, Register.SP, ImmediateIntOperand(stackOffset)))
        }
        /** Translates the condition after the loop body.*/
        instructions.add(conditionLabel)
        instructions.addAll(visit(ast.expr))
        instructions.add(CompareInstruction(programState.recentlyUsedCalleeReg(), ImmediateIntOperand(1)))
        instructions.add(BranchInstruction(Condition.EQ, bodyLabel, false))
        programState.freeCalleeReg()
        return instructions
    }


    /**
     * Translates an array element AST, e.g. a[3] where int x = a[3]
     */
    fun visitArrayElemAST(ast: ArrayElemAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        val stackReg = programState.getFreeCalleeReg()

        /** Computes offset to push down the stack pointer */
        var stackOffset = findIdentOffset(ast.symbolTable, ast.ident.name)
        stackOffset += checkParamOffset(ast.symbolTable, ast.ident.name) + ast.symbolTable.callOffset
        instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, stackReg, Register.SP, ImmediateIntOperand(stackOffset)))

        ast.listOfIndex.forEach {
            instructions.addAll(visit(it))

            instructions.add(LoadInstruction(Condition.AL, RegisterMode(stackReg), stackReg))
            instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(programState.recentlyUsedCalleeReg())))
            instructions.add(MoveInstruction(Condition.AL, Register.R1, RegisterOperand(stackReg)))
            instructions.add(BranchInstruction(Condition.AL, RuntimeErrors.checkArrayBoundsLabel, true))
            ProgramState.runtimeErrors.addArrayBoundsCheck()

            // Add pointer offset
            instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, stackReg, stackReg, ImmediateIntOperand(4)))

            val identType = ast.ident.getType(ast.symbolTable)
            if ((identType is ArrayTypeAST) && ((identType.type is BaseTypeAST && identType.type.type == BaseType.CHAR)
                        || (identType.type is BaseTypeAST && identType.type.type == BaseType.BOOL))) {
                instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, stackReg, stackReg,
                    RegisterOperand(programState.recentlyUsedCalleeReg())))
            } else {
                val multiplyByFour = 2
                instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, stackReg, stackReg,
                    RegisterOperandWithShift(programState.recentlyUsedCalleeReg(), ShiftType.LSL, multiplyByFour)))
            }
            programState.freeCalleeReg()
        }
        return instructions
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
            instructions.addAll(visit(expr))
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