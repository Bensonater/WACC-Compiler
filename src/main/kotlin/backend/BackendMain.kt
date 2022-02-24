package backend

import frontend.ast.ASTNode
import frontend.ast.ProgramAST
import java.io.File

fun main(ast: ASTNode, fileName: String) {
    val instructions = GenerateASTVisitor().visit(ast as ProgramAST)



    /**
     * Creates an assembly file and write the instructions
     */
    val file = File("$fileName.s")
    file.writeText(printCode(instructions!!))
}