package backend

import backend.instruction.Instruction
import frontend.ast.*
import frontend.ast.literal.*
import frontend.ast.statement.*

class GenerateASTVisitor {

    fun visit(ast: ASTNode) {
        //return ast.accept(this)
    }

    fun visitProgramAST(ast: ProgramAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitFuncAST(ast: FuncAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitParamAST(ast: ParamAST): List<Instruction> {
        return mutableListOf()
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

    fun visitSkipAST(ast: SkipAST): List<Instruction> {
        return mutableListOf()
    }

    fun visitStatMultiAST(ast: StatMultiAST): List<Instruction> {
        return mutableListOf()
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