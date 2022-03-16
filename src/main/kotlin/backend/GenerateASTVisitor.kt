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

class GenerateASTVisitor (val programState: ProgramState): ASTVisitor<List<Instruction>> {

    /**
     * Translate a program AST and sets the initial directives for main,
     * adds data directive, runtime errors and the library functions.
     */
    override fun visitProgramAST(ast: ProgramAST): List<Instruction> {
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
     * Translate the function AST and allocate stack for new scope.
     */
    override fun visitFuncAST(ast: FuncAST): List<Instruction> {
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
    override fun visitParamAST(ast: ParamAST): List<Instruction> {
        return emptyList()
    }

    /**
     * Translate the binary operator expression AST.
     * Loads result to recentlyUsedCalleeReg
     */
    override fun visitBinOpExprAST(ast: BinOpExprAST): List<Instruction> {
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

        // Load content at address back to register for elem expressions
        loadAddress(ast.expr1, instructions, reg1)
        loadAddress(ast.expr2, instructions, reg2)

        // Add instructions based on the type of operation.
        when (ast.binOp) {
            IntBinOp.PLUS, IntBinOp.MINUS -> {
                val instr = if (ast.binOp == IntBinOp.PLUS) {
                    ArithmeticInstrType.ADD
                } else {
                    ArithmeticInstrType.SUB
                }
                if (accumUsed) {
                    if (ast.pointerArithmetic) {
                        instructions.add(ArithmeticInstruction(instr, reg1, reg2, RegisterOperandWithShift(reg1, ShiftType.LSL, ast.shiftOffset), true))
                    } else {
                        instructions.add(ArithmeticInstruction(instr, reg1, reg2, RegisterOperand(reg1), true))
                    }
                } else {
                    if (ast.pointerArithmetic) {
                        instructions.add(ArithmeticInstruction(instr, reg1, reg1, RegisterOperandWithShift(reg2, ShiftType.LSL, ast.shiftOffset), true))
                    } else {
                        instructions.add(ArithmeticInstruction(instr, reg1, reg1, RegisterOperand(reg2), true))
                    }
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
     * Loads real value to recentlyUsedCalleeReg
     */
    override fun visitUnOpExprAST(ast: UnOpExprAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        // Visit the expression
        instructions.addAll(visit(ast.expr))
        val reg = programState.recentlyUsedCalleeReg()

        // Load content at address back to register for elem expressions
        loadAddress(ast.expr, instructions, reg)

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
            UnOp.REF -> {
                if (ast.expr is IdentAST) {
                    val offset = findIdentOffset(ast.symbolTable, ast.expr.name) + ast.symbolTable.callOffset
                    instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, reg, Register.SP, ImmediateIntOperand(offset)))
                }
            }
            UnOp.DEREF -> {
                // Perform runtime error null reference check
                instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg)))
                instructions.add(BranchInstruction(Condition.AL, RuntimeErrors.nullReferenceLabel, true))
                ProgramState.runtimeErrors.addNullReferenceCheck()

                val baseType = (ast.expr.getType(ast.symbolTable) as PointerTypeAST).type
                val memType = if (baseType is BaseTypeAST && (baseType.type == BaseType.BOOL || baseType.type == BaseType.CHAR))
                    Memory.SB else null
                // Load real value from memory.
                instructions.add(LoadInstruction(Condition.AL, RegisterMode(reg), reg, memType))
            }
            else -> {}
        }
        return instructions
    }

    /**
     * Translate ident variable AST and find offset on stack for that variable
     * Loads real value to recentlyUsedCalleeReg
     */
    override fun visitIdentAST(ast: IdentAST): List<Instruction> {
        val offset = findIdentOffset(ast.symbolTable, ast.name) + ast.symbolTable.callOffset
        val typeAST = ast.getType(ast.symbolTable)
        val isBoolOrChar = typeAST is BaseTypeAST && (typeAST.type == BaseType.BOOL || typeAST.type == BaseType.CHAR)
        val memoryType = if (isBoolOrChar) Memory.SB else null
        return listOf(LoadInstruction(Condition.AL, RegisterModeWithOffset(Register.SP, offset),
            programState.getFreeCalleeReg(), memoryType))
    }

    /**
     * Translate Pair Element AST for indexing a pair
     * Loads element address into recentlyUsedCalleeReg
     */
    override fun visitPairElemAST(ast: PairElemAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        instructions.addAll(visit(ast.expr))
        val reg = programState.recentlyUsedCalleeReg()

        // Load content at address back to register for elem expressions
        loadAddress(ast.expr, instructions, reg)

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

    /**
     * Translate New Pair AST for declaring a new pair
     */
    override fun visitNewPairAST(ast: NewPairAST): List<Instruction> {
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

    /**
     * Helper function to malloc each pair elem
     */
    private fun mallocPairAST(ast: ExprAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        instructions.addAll(visit(ast))
        val astType = ast.getType(ast.symbolTable)!!
        instructions.add(LoadInstruction(Condition.AL, ImmediateInt(astType.size), Register.R0))
        instructions.add(BranchInstruction(Condition.AL, GeneralLabel(Funcs.MALLOC.toString()), true))

        val reg = programState.recentlyUsedCalleeReg()
        // Load content at address back to register for elem expressions
        val memoryType = loadAddress(ast, instructions, reg)

        instructions.add(StoreInstruction(RegisterMode(Register.R0), reg, memoryType))
        programState.freeCalleeReg()

        return instructions
    }

    /**
     * Generate code for assign statements
     */
    override fun visitAssignAST(ast: AssignAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()

        instructions.addAll(visit(ast.assignRhs))
        val reg = programState.recentlyUsedCalleeReg()
        if (ast.assignRhs is StrLiterAST) {
            ast.label = ProgramState.dataDirective.toStringLabel(ast.assignRhs.value)
        }

        // Load content at address back to register for elem expressions
        val memoryType = loadAddress(ast.assignRhs, instructions, reg)

        when (ast.assignLhs) {
            is IdentAST -> {
                val offset = findIdentOffset(ast.symbolTable, ast.assignLhs.name)
                instructions.add(StoreInstruction(RegisterModeWithOffset(Register.SP, offset), reg, memoryType))
            }
            is ArrayElemAST, is PairElemAST, is PointerElemAST -> {
                instructions.addAll(visit(ast.assignLhs))
                instructions.add(StoreInstruction(RegisterMode(programState.recentlyUsedCalleeReg()), reg, memoryType))
                programState.freeCalleeReg()
            }
        }
        programState.freeCalleeReg()
        return instructions
    }

    /**
     * Generate code for begin blocks and create a new scope.
     */
    override fun visitBeginAST(ast: BeginAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        val stackOffset = allocateStack(ast.symbolTable, instructions)
        ast.stats.forEach { instructions.addAll(visit(it)) }
        deallocateStack(stackOffset, instructions)
        return instructions
    }

    /**
     * Generate code for call statements and store arguments on stack
     */
    override fun visitCallAST(ast: CallAST): List<Instruction> {
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

            // Load content at address back to register for elem expressions
            val memoryType = loadAddress(arg, instructions, reg)

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

    /**
     * Generate code for variable declarations and store value on stack
     */
    override fun visitDeclareAST(ast: DeclareAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        instructions.addAll(visit(ast.assignRhs))

        if (ast.assignRhs is StrLiterAST) {
            ast.label = ProgramState.dataDirective.toStringLabel(ast.assignRhs.value)
        }
        ast.symbolTable.currOffset -= ast.type.size
        val reg = programState.recentlyUsedCalleeReg()

        // Load content at address back to register for elem expressions
        val memoryType = loadAddress(ast.assignRhs, instructions, reg)

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

    /**
     * Generate code for if statements and check for return statements
     */
    override fun visitIfAST(ast: IfAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        val elseLabel = programState.getNextLabel()
        val finalLabel = programState.getNextLabel()

        instructions.addAll(visit(ast.expr))
        val reg = programState.recentlyUsedCalleeReg()

        // Load content at address back to register for elem expressions
        loadAddress(ast.expr, instructions, reg)

        instructions.add(CompareInstruction(reg, ImmediateIntOperand(0)))
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

    /**
     * Generate code for read statements and assign value to variable on stack
     */
    override fun visitReadAST(ast: ReadAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        if (ast.assignLhs is IdentAST) {
            instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, Register.R0, Register.SP,
                ImmediateIntOperand(findIdentOffset(ast.symbolTable,ast.assignLhs.name))))
        } else {
            instructions.addAll(visit(ast.assignLhs))
            instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(programState.recentlyUsedCalleeReg())))
        }

        // Reads library function
        when ((ast.assignLhs.getType(ast.symbolTable) as BaseTypeAST).type) {
            BaseType.INT -> {
                instructions.add(BranchInstruction(Condition.AL, GeneralLabel(CallFunc.READ_INT.toString()), true))
                ProgramState.library.addCode(CallFunc.READ_INT)
            }
            BaseType.CHAR -> {
                instructions.add(BranchInstruction(Condition.AL, GeneralLabel(CallFunc.READ_CHAR.toString()), true))
                ProgramState.library.addCode(CallFunc.READ_CHAR)
            }
            else -> {}
        }
        return instructions
    }

    /**
     * No code generation is required to translate SkipAST.
     */
    override fun visitSkipAST(ast: SkipAST): List<Instruction> {
        return emptyList()
    }

    /**
     * Translates multiple statements between BEGIN and END commands.
     */
    override fun visitStatMultiAST(ast: StatMultiAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        ast.stats.forEach { instructions.addAll(visit(it)) }
        return instructions
    }

    /**
     * Translates a single statement
     */
    override fun visitStatSimpleAST(ast: StatSimpleAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        instructions.addAll(visit(ast.expr))

        val reg = programState.recentlyUsedCalleeReg()
        val exprType = ast.expr.getType(ast.symbolTable)!!

        // Load content at address back to register for elem expressions
        loadAddress(ast.expr, instructions, reg)
        instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg)))

        when (ast.command) {
            Command.EXIT -> {
                instructions.add(BranchInstruction(Condition.AL, GeneralLabel("exit"), true))
                programState.freeAllCalleeRegs()
            }
            Command.PRINT, Command.PRINTLN -> {
                when (exprType) {
                    is BaseTypeAST -> {
                        // Stores base types and their respective function calls
                        val lookupPrintInstr = hashMapOf(
                            Pair(BaseType.INT, CallFunc.PRINT_INT),
                            Pair(BaseType.BOOL, CallFunc.PRINT_BOOL),
                            Pair(BaseType.STRING, CallFunc.PRINT_STRING)
                        )
                        if (exprType.type == BaseType.CHAR){
                            instructions.add(BranchInstruction(Condition.AL, GeneralLabel(Funcs.PUTCHAR.toString()), true))
                        } else{
                            // Looks up the type and adds the function call
                            val printInstr = lookupPrintInstr[exprType.type]!!
                            ProgramState.library.addCode(printInstr)
                            instructions.add(BranchInstruction(Condition.AL, GeneralLabel(printInstr.toString()), true))
                        }
                    }
                    is ArrayTypeAST -> {
                        // Prints string if the array is made up of characters, otherwise print the references
                        if (exprType.type is BaseTypeAST && (exprType.type.type == BaseType.CHAR)) {
                            instructions.add(BranchInstruction(Condition.AL, GeneralLabel(CallFunc.PRINT_STRING.toString()), true))
                            ProgramState.library.addCode(CallFunc.PRINT_STRING)
                        } else {
                            instructions.add(BranchInstruction(Condition.AL, GeneralLabel(CallFunc.PRINT_REFERENCE.toString()), true))
                            ProgramState.library.addCode(CallFunc.PRINT_REFERENCE)
                        }
                    }
                    // Print references for pairs, pointers and null types
                    is PairTypeAST, is PointerTypeAST, is ArbitraryTypeAST -> {
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
                // Determine which type of data structure to free
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
                moveStackPointer(ArithmeticInstrType.ADD, checkFuncOffset(ast.symbolTable), instructions)
                instructions.add(PopInstruction(Register.PC))
                programState.freeAllCalleeRegs()
            }
        }
        return instructions
    }

    /**
     * Generate code for while statements and create a new scope on stack
     */
    override fun visitWhileAST(ast: WhileAST): List<Instruction> {
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

        val reg = programState.recentlyUsedCalleeReg()
        // Load content at address back to register for elem expressions
        loadAddress(ast.expr, instructions, reg)

        instructions.add(CompareInstruction(reg, ImmediateIntOperand(1)))
        instructions.add(BranchInstruction(Condition.EQ, bodyLabel, false))
        programState.freeCalleeReg()
        return instructions
    }

    /**
     * Translates an array element AST, e.g. a[3] where int x = a[3]
     * Loads element address into recentlyUsedCalleeReg
     */
    override fun visitArrayElemAST(ast: ArrayElemAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        val stackReg = programState.getFreeCalleeReg()

        /** Computes offset to push down the stack pointer */
        val stackOffset = findIdentOffset(ast.symbolTable, ast.ident.name) + ast.symbolTable.callOffset
        instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, stackReg, Register.SP, ImmediateIntOperand(stackOffset)))

        ast.listOfIndex.forEach {
            instructions.addAll(visit(it))
            val reg = programState.recentlyUsedCalleeReg()
            // Load content at address back to register for elem expressions
            loadAddress(it, instructions, reg)

            instructions.add(LoadInstruction(Condition.AL, RegisterMode(stackReg), stackReg))
            instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg)))
            instructions.add(MoveInstruction(Condition.AL, Register.R1, RegisterOperand(stackReg)))
            instructions.add(BranchInstruction(Condition.AL, RuntimeErrors.checkArrayBoundsLabel, true))
            ProgramState.runtimeErrors.addArrayBoundsCheck()

            // Add pointer offset
            instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, stackReg, stackReg, ImmediateIntOperand(SIZE_OF_POINTER)))

            val identType = ast.ident.getType(ast.symbolTable)

            if ((identType is ArrayTypeAST) && (identType.type is BaseTypeAST &&
                        (identType.type.type == BaseType.BOOL || identType.type.type == BaseType.CHAR))) {
                instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, stackReg, stackReg, RegisterOperand(reg)))
            } else {
                val multiplyByFour = 2
                instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, stackReg, stackReg,
                    RegisterOperandWithShift(reg, ShiftType.LSL, multiplyByFour)))
            }
            programState.freeCalleeReg()
        }
        return instructions
    }

    /**
     * Translate an array literal AST, e.g. [19, 21, 3, a, 7] where a = 30
     */
    override fun visitArrayLiterAST(ast: ArrayLiterAST): List<Instruction> {
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
            val reg = programState.recentlyUsedCalleeReg()
            // Load content at address back to register for elem expressions
            loadAddress(expr, instructions, reg)

            if ((expr is CharLiterAST) || (expr is BoolLiterAST)) {
                memoryType = Memory.B
            }
            instructions.add(StoreInstruction(RegisterModeWithOffset(stackReg, sizeOfInt + (index * elemSize)), reg, memoryType))
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
    override fun visitBoolLiterAST(ast: BoolLiterAST): List<Instruction> {
        return visitLiterHelper(ImmediateBoolOperand(ast.value), false)
    }

    /**
     * Translate a character literal AST.
     */
    override fun visitCharLiterAST(ast: CharLiterAST): List<Instruction> {
        return visitLiterHelper(ImmediateCharOperand(ast.value), false)
    }

    /**
     * Translate an integer literal AST.
     */
    override fun visitIntLiterAST(ast: IntLiterAST): List<Instruction> {
        return visitLiterHelper(ImmediateInt(ast.value), true)
    }

    /**
     * Translate a null pair literal AST.
     */
    override fun visitNullPairLiterAST(ast: NullPairLiterAST): List<Instruction> {
        return visitLiterHelper(ImmediateInt(0), true)
    }

    /**
     * Translate a string literal AST.
     */
    override fun visitStrLiterAST(ast: StrLiterAST): List<Instruction> {
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

    /**
     * Generate code for a pointer on LHS e.g. *a = 1
     * Loads pointer address into recentlyUsedCalleeReg
     */
    override fun visitPointerElemAST(ast: PointerElemAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        instructions.addAll(visit(ast.expr))
        val reg = programState.recentlyUsedCalleeReg()
        // Load content at address back to register for elem expressions
        loadAddress(ast.expr, instructions, reg)

        // Perform runtime error null reference check
        instructions.add(MoveInstruction(Condition.AL, Register.R0, RegisterOperand(reg)))
        instructions.add(BranchInstruction(Condition.AL, RuntimeErrors.nullReferenceLabel, true))
        ProgramState.runtimeErrors.addNullReferenceCheck()
        return instructions
    }

    private fun loadAddress(expr: ASTNode, instructions: MutableList<Instruction>, reg: Register): Memory? {
        val type = expr.getType(expr.symbolTable)
        val isBoolOrChar = type is BaseTypeAST && (type.type == BaseType.BOOL || type.type == BaseType.CHAR)
        val memoryType = if (isBoolOrChar) Memory.B else null

        // Loads content at address back to register
        if (expr is PairElemAST || expr is ArrayElemAST || expr is PointerElemAST) {
            instructions.add(LoadInstruction(Condition.AL, RegisterMode(reg), reg, memoryType))
        }

        return memoryType
    }
}