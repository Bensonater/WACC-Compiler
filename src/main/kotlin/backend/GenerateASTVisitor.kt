package backend

import backend.addressingmodes.ImmediateInt
import backend.enums.Register
import backend.addressingmodes.ImmediateIntOperand
import backend.addressingmodes.RegisterOperand
import backend.enums.Condition
import backend.instruction.*
import frontend.SymbolTable
import frontend.ast.*
import frontend.ast.literal.*
import frontend.ast.statement.*
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
//        val cLib = ProgramState.cLib.translate()

        return data + instructions // + cLib
    }

    fun visitFuncAST(ast: FuncAST): List<Instruction> {
        val instructions = mutableListOf<Instruction>()
        instructions.add(FunctionLabel(ast.ident.name))
        instructions.add(PushInstruction(Register.LR))
        val offset = getStackOffset(ast.symbolTable)

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
                    instructions.add(ArithmeticInstruction(ArithmeticInstrType.ADD, reg1, reg2, RegisterOperand(reg1)))
                }
            }
            IntBinOp.MINUS -> {

            }
            IntBinOp.MULT -> {

            }
            IntBinOp.DIV -> {

            }
            IntBinOp.MOD -> {

            }

        }

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