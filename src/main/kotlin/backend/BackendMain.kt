package backend

import frontend.ast.ASTNode
import frontend.ast.ProgramAST

fun main(ast: ASTNode) {
    val instructions = GenerateASTVisitor(ProgramState()).visit(ast as ProgramAST)
    return printCode(instructions!!)
}