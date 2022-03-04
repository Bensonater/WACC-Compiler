package backend

import backend.addressingmodes.*
import backend.enums.Condition
import backend.enums.Memory
import backend.enums.Register
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

    /**
     * Translate a program AST and sets the initial directives for main,
     * adds data directive, runtime errors and the library functions.
     */
    fun visitProgramAST(ast: ProgramAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()

        instructions.add(DirectiveInstruction("text"))
        instructions.add(DirectiveInstruction("global main"))

        val functionsInstructions =
            ast.funcList.stream().map { GenerateASTVisitor(programState).visit(it) }
                .collect(Collectors.toList())

        functionsInstructions.forEach { instructions.addAll(it!!) }

        instructions.add(GeneralLabel("main"))
        instructions.add(PushInstruction(Register.LR))

        val stackOffset = allocateStack(ast.symbolTable, instructions)

        ast.stats.forEach { instructions.addAll(visit(it)) }

        deallocateStack(stackOffset, instructions)

        instructions.add(LoadInstruction(Condition.AL, ImmediateInt(0), Register.R0))
        instructions.add(EndInstruction())
        instructions.add(DirectiveInstruction("ltorg"))

        val data = ProgramState.dataDirective.translate()
        val runtimeErrors = ProgramState.runtimeErrors.translate()
        val library = ProgramState.library.translate()

        return data + instructions + runtimeErrors + library
    }

    /**
     * Translate the function AST.
     */
    fun visitFuncAST(ast: FuncAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        // Create label with the function name (preceded with "f_")
        instructions.add(FunctionLabel(ast.ident.name))
        instructions.add(PushInstruction(Register.LR))
        // Allocate space in the stack for the variables in the function.
        val stackOffset = allocateStack(ast.symbolTable, instructions)
        // Translate all the statements in the function.
        ast.stats.forEach { instructions.addAll(visit(it)) }
        // Check if the last statement is an if else statement and direct their return or exit commands to the
        // appropriate locations.
        val lastStat = ast.stats.last()
        if (!(((lastStat is IfAST) && lastStat.thenReturns && lastStat.elseReturns)
                    || ((lastStat is StatSimpleAST) && lastStat.command == Command.EXIT))) {
            deallocateStack(stackOffset, instructions)
            instructions.add(PopInstruction(Register.PC))
        }
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

    /**
     * Translate the binary operator expression AST.
     */
    fun visitBinOpExprAST(ast: BinOpExprAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()

        // Visit both expressions
        instructions.addAll(visit(ast.expr1))
        var reg1 = programState.recentlyUsedCalleeReg()
        instructions.addAll(visit(ast.expr2))
        var reg2 = programState.recentlyUsedCalleeReg()

        // Use the accumulator if there are no free registers.
        var accumUsed = false
        if (reg1 == Register.NONE || reg1 == Register.R11) {
            accumUsed = true
            reg1 = Register.R11
            reg2 = Register.R12
            instructions.add(PopInstruction(Register.R12))
        }
        // Add instructions based on the type of operation.
        when (ast.binOp) {
            IntBinOp.PLUS, IntBinOp.MINUS -> {
                val instr = if (ast.binOp == IntBinOp.PLUS) {
                    ArithmeticInstrType.ADD
                } else {
                    ArithmeticInstrType.SUB
                }
                if (accumUsed) {
                    instructions.add(ArithmeticInstruction(instr, reg1, reg2, RegisterOperand(reg1), true))
                } else {
                    instructions.add(ArithmeticInstruction(instr, reg1, reg1, RegisterOperand(reg2), true))
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
                    instructions.add(LogicInstruction(LogicOperation.ORR, reg1, reg2, RegisterOperand(reg1)))
                } else {
                    instructions.add(LogicInstruction(LogicOperation.ORR, reg1, reg1, RegisterOperand(reg2)))
                }
            }
        }
        if (!accumUsed) {
            programState.freeCalleeReg()
        }
        return instructions
    }


    /**
     * Translate the unary operator AST.
     */
    fun visitUnOpExprAST(ast: UnOpExprAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        // Visit the expression
        instructions.addAll(visit(ast.expr))
        val reg = programState.recentlyUsedCalleeReg()

        // Add instructions based on the type of unary operator
        when (ast.unOp) {
            UnOp.NOT -> {
                instructions.add(LogicInstruction(LogicOperation.EOR, reg, reg, ImmediateIntOperand(1)))
            }
            UnOp.MINUS -> {
                instructions.add(ArithmeticInstruction(ArithmeticInstrType.RSB, reg, reg, ImmediateIntOperand(0), true))
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
        val offset = findIdentOffset(ast.symbolTable, ast.name) + ast.symbolTable.callOffset
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
            instructions.add(LoadInstruction(Condition.AL, RegisterModeWithOffset(reg, SIZE_OF_POINTER), reg))
        }
        return instructions
    }

    fun visitNewPairAST(ast: NewPairAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()

        // Malloc space for two pointers to the first and second elements
        instructions.add(LoadInstruction(Condition.AL, ImmediateInt(2 * SIZE_OF_POINTER), Register.R0))
        instructions.add(BranchInstruction(Condition.AL, GeneralLabel(Funcs.MALLOC.toString()), true))
        val stackReg = programState.getFreeCalleeReg()
        instructions.add(MoveInstruction(Condition.AL, stackReg, RegisterOperand(Register.R0)))

        // Malloc first element
        instructions.addAll(mallocPairAST(ast.fst))
        instructions.add(StoreInstruction(RegisterMode(stackReg), Register.R0))

        // Malloc second element
        instructions.addAll(mallocPairAST(ast.snd))
        instructions.add(StoreInstruction(RegisterModeWithOffset(stackReg, SIZE_OF_POINTER), Register.R0))

        return instructions
    }

    private fun mallocPairAST(ast: ExprAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        instructions.addAll(visit(ast))
        val astType = ast.getType(ast.symbolTable)!!
        instructions.add(LoadInstruction(Condition.AL, ImmediateInt(astType.size), Register.R0))
        instructions.add(BranchInstruction(Condition.AL, GeneralLabel(Funcs.MALLOC.toString()), true))

        val isBoolOrChar = astType is BaseTypeAST && (astType.type == BaseType.BOOL || astType.type == BaseType.CHAR)
        val memoryType = if (isBoolOrChar) Memory.B else null

        instructions.add(StoreInstruction(RegisterMode(Register.R0), programState.recentlyUsedCalleeReg(), memoryType))
        programState.freeCalleeReg()

        return instructions
    }

    fun visitAssignAST(ast: AssignAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()

        instructions.addAll(visit(ast.assignRhs))
        val reg = programState.recentlyUsedCalleeReg()
        if (ast.assignRhs is StrLiterAST) {
            ast.label = ProgramState.dataDirective.toStringLabel(ast.assignRhs.value)
        }

        val rhsType = ast.assignRhs.getType(ast.symbolTable)
        val isBoolOrChar = rhsType is BaseTypeAST && (rhsType.type == BaseType.BOOL || rhsType.type == BaseType.CHAR)
        val memoryType = if (isBoolOrChar) Memory.B else null

        if (ast.assignRhs is PairElemAST) {
            instructions.add(LoadInstruction(Condition.AL, RegisterMode(reg), reg))
        }

        when (ast.assignLhs) {
            is IdentAST -> {
                val offset = findIdentOffset(ast.symbolTable, ast.assignLhs.name)
                instructions.add(StoreInstruction(RegisterModeWithOffset(Register.SP, offset), reg, memoryType))
            }
            is ArrayElemAST, is PairElemAST -> {
                instructions.addAll(visit(ast.assignLhs))
                instructions.add(StoreInstruction(RegisterMode(programState.recentlyUsedCalleeReg()), reg, memoryType))
                programState.freeCalleeReg()
            }
        }
        programState.freeCalleeReg()
        return instructions
    }

    fun visitBeginAST(ast: BeginAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        val stackOffset = allocateStack(ast.symbolTable, instructions)
        ast.stats.forEach { instructions.addAll(visit(it)) }
        deallocateStack(stackOffset, instructions)
        return instructions
    }

    fun visitCallAST(ast: CallAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()

        var totalBytes = 0
        val argTypesReversed = ast.args.map { it.getType(ast.symbolTable) }.reversed()
        val negativeCallStackOffset = -1
        for ((index, arg) in ast.args.reversed().withIndex()) {
            instructions.addAll(visit(arg))
            val reg = programState.recentlyUsedCalleeReg()
            val argType = argTypesReversed[index]
            val size = argType!!.size
            totalBytes += size
            ast.symbolTable.callOffset = totalBytes
            val isBoolOrChar =
                argType is BaseTypeAST && (argType.type == BaseType.BOOL || argType.type == BaseType.CHAR)
            val memoryType = if (isBoolOrChar) Memory.B else null
            if (arg is ArrayElemAST) {
                instructions.add(LoadInstruction(Condition.AL, RegisterMode(reg), reg))
            }
            instructions.add(
                StoreInstruction(
                    RegisterModeWithOffset(Register.SP, negativeCallStackOffset * size, true),
                    reg,
                    memoryType
                )
            )
            programState.freeCalleeReg()
        }
        ast.symbolTable.callOffset = 0

        val funcLabel = FunctionLabel(ast.ident.name)
        instructions.add(BranchInstruction(Condition.AL, funcLabel, true))
        moveStackPointer(ArithmeticInstrType.ADD, totalBytes, instructions)
        instructions.add(MoveInstruction(Condition.AL, programState.getFreeCalleeReg(), RegisterOperand(Register.R0)))
        return instructions
    }

    fun visitDeclareAST(ast: DeclareAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        instructions.addAll(visit(ast.assignRhs))

        if (ast.assignRhs is StrLiterAST) {
            ast.label = ProgramState.dataDirective.toStringLabel(ast.assignRhs.value)
        }
        ast.symbolTable.currOffset -= ast.type.size
        val isBoolOrChar = ast.type is BaseTypeAST && (ast.type.type == BaseType.BOOL || ast.type.type == BaseType.CHAR)
        val memoryType = if (isBoolOrChar) Memory.B else null
        val reg = programState.recentlyUsedCalleeReg()

        if (ast.type is PairTypeAST) {
            if (ast.assignRhs !is NewPairAST && ast.assignRhs !is ArrayElemAST && ast.assignRhs !is IdentAST &&
                ast.assignRhs !is NullPairLiterAST && ast.assignRhs !is CallAST && ast.assignRhs !is PairElemAST
            ) {
                instructions.add(LoadInstruction(Condition.AL, RegisterMode(reg), reg))
            }
        }

        if (ast.assignRhs is PairElemAST || ast.assignRhs is ArrayElemAST) {
            instructions.add(LoadInstruction(Condition.AL, RegisterMode(reg), reg, memoryType))
        }
        instructions.add(
            StoreInstruction(
                RegisterModeWithOffset(Register.SP, ast.symbolTable.currOffset),
                reg,
                memoryType
            )
        )
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
        var stackOffset = allocateStack (ast.thenSymbolTable, instructions)

        ast.thenStat.forEach { instructions.addAll(visit(it)) }

        val lastThenStat = ast.thenStat.last()
        val thenReturns = lastThenStat is StatSimpleAST &&
                (lastThenStat.command == Command.RETURN || lastThenStat.command == Command.EXIT)
        ast.thenReturns = thenReturns
        deallocateStack(stackOffset, instructions)

        instructions.add(BranchInstruction(Condition.AL, finalLabel, false))
        instructions.add(elseLabel)
        stackOffset = allocateStack (ast.elseSymbolTable, instructions)

        ast.elseStat.forEach { instructions.addAll(visit(it)) }

        val lastElseStat = ast.elseStat.last()
        val elseReturns = lastElseStat is StatSimpleAST &&
                (lastElseStat.command == Command.RETURN || lastElseStat.command == Command.EXIT)
        ast.elseReturns = elseReturns
        deallocateStack(stackOffset, instructions)
        instructions.add(finalLabel)

        return instructions
    }

    fun visitReadAST(ast: ReadAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        when (ast.assignLhs) {
            is IdentAST -> {
                instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, Register.R4, Register.SP,
                    ImmediateIntOperand(findIdentOffset(ast.symbolTable,ast.assignLhs.name))))
            }
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
        when ((ast.assignLhs.getType(ast.symbolTable) as BaseTypeAST).type) {
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
            val isBoolOrChar = exprType is BaseTypeAST && (exprType.type == BaseType.BOOL || exprType.type == BaseType.CHAR)
            val memoryType = if (isBoolOrChar) Memory.SB else null
            instructions.add(LoadInstruction(Condition.AL, RegisterMode(reg), reg, memoryType))
        }
        when (ast.command) {
            Command.EXIT -> {
                instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg)))
                instructions.add(BranchInstruction(Condition.AL, GeneralLabel("exit"), true))
                programState.freeAllCalleeRegs()
            }
            Command.PRINT, Command.PRINTLN -> {
                instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg)))
                when (exprType) {
                    is BaseTypeAST -> {
                        val lookupPrintInstr = hashMapOf(
                            Pair(BaseType.INT, CallFunc.PRINT_INT),
                            Pair(BaseType.BOOL, CallFunc.PRINT_BOOL),
                            Pair(BaseType.STRING, CallFunc.PRINT_STRING)
                        )
                        if (exprType.type == BaseType.CHAR){
                            instructions.add(BranchInstruction(Condition.AL, GeneralLabel(Funcs.PUTCHAR.toString()), true))
                        }
                        else{
                            val printInstr = lookupPrintInstr[exprType.type]!!
                            ProgramState.library.addCode(printInstr)
                            instructions.add(BranchInstruction(Condition.AL, GeneralLabel(printInstr.toString()), true))
                        }
                    }
                    is ArrayTypeAST -> {
                        if (exprType.type is BaseTypeAST && (exprType.type.type == BaseType.CHAR)) {
                            instructions.add(BranchInstruction(Condition.AL, GeneralLabel(CallFunc.PRINT_STRING.toString()), true))
                            ProgramState.library.addCode(CallFunc.PRINT_STRING)
                        } else {
                            instructions.add(BranchInstruction(Condition.AL, GeneralLabel(CallFunc.PRINT_REFERENCE.toString()), true))
                            ProgramState.library.addCode(CallFunc.PRINT_REFERENCE)
                        }
                    }
                    is PairTypeAST, is ArbitraryTypeAST -> {
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
                val freeType = if (exprType is ArrayTypeAST) {
                    CallFunc.FREE_ARRAY
                } else {
                    CallFunc.FREE_PAIR
                }

                instructions.add(BranchInstruction(Condition.AL, GeneralLabel(freeType.toString()), true))
                ProgramState.library.addCode(freeType)
                programState.freeCalleeReg()
            }
            Command.RETURN -> {
                instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg)))
                moveStackPointer(ArithmeticInstrType.ADD, checkFuncOffset(ast.symbolTable), instructions)
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
        val stackOffset = allocateStack (ast.bodySymbolTable, instructions)
        /** Translates all the statements within the while loop body */
        for (stat in ast.stats) {
            instructions.addAll(visit(stat))
        }
        deallocateStack(stackOffset, instructions)
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
        val stackOffset = findIdentOffset(ast.symbolTable, ast.ident.name) + ast.symbolTable.callOffset
        instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, stackReg, Register.SP, ImmediateIntOperand(stackOffset)))

        ast.listOfIndex.forEach {
            instructions.addAll(visit(it))
            instructions.add(LoadInstruction(Condition.AL, RegisterMode(stackReg), stackReg))
            instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(programState.recentlyUsedCalleeReg())))
            instructions.add(MoveInstruction(Condition.AL, Register.R1, RegisterOperand(stackReg)))
            instructions.add(BranchInstruction(Condition.AL, RuntimeErrors.checkArrayBoundsLabel, true))
            ProgramState.runtimeErrors.addArrayBoundsCheck()

            // Add pointer offset
            instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, stackReg, stackReg, ImmediateIntOperand(SIZE_OF_POINTER)))

            val identType = ast.ident.getType(ast.symbolTable)

            if ((identType is ArrayTypeAST) && (identType.type is BaseTypeAST &&
                        (identType.type.type == BaseType.BOOL || identType.type.type == BaseType.CHAR))) {
                instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, stackReg, stackReg, RegisterOperand(programState.recentlyUsedCalleeReg())))
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
        val elemSize = (ast.getType(ast.symbolTable) as ArrayTypeAST).type.size

        val sizeOfInt = 4
        instructions.add(LoadInstruction(Condition.AL, ImmediateInt(elemSize * ast.vals.size + sizeOfInt), Register.R0))
        instructions.add(BranchInstruction(Condition.AL, GeneralLabel("malloc"), true))
        val stackReg = programState.getFreeCalleeReg()
        instructions.add(MoveInstruction(Condition.AL, stackReg, RegisterOperand(Register.R0)))

        var memoryType: Memory? = null
        for ((index, expr) in ast.vals.withIndex()) {
            instructions.addAll(visit(expr))
            if ((expr is CharLiterAST) || (expr is BoolLiterAST)) {
                memoryType = Memory.B
            }
            instructions.add(StoreInstruction(RegisterModeWithOffset(stackReg, sizeOfInt + (index * elemSize)), programState.recentlyUsedCalleeReg(), memoryType))
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