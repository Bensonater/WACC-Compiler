package frontend.ast

import backend.GenerateASTVisitor
import backend.instruction.Instruction
import frontend.SymbolTable
import frontend.ast.statement.StatAST
import frontend.ast.type.BaseType
import frontend.ast.type.BaseTypeAST
import frontend.semanticErrorHandler
import org.antlr.v4.runtime.ParserRuleContext

/**
 * AST node representing the entire program with a function list and body statement.
 * Checks each function is not already defined and records in the symbol table.
 */
class ProgramAST(val ctx: ParserRuleContext, val funcList: List<FuncAST>, val stat: StatAST) : ASTNode(ctx) {
    /* Inserts all base type into Symbol Table */
    init {
        this.symbolTable = SymbolTable()
        symbolTable.put("int", BaseTypeAST(ctx, BaseType.INT))
        symbolTable.put("bool", BaseTypeAST(ctx, BaseType.BOOL))
        symbolTable.put("char", BaseTypeAST(ctx, BaseType.CHAR))
        symbolTable.put("string", BaseTypeAST(ctx, BaseType.STRING))
    }

    override fun check(symbolTable: SymbolTable): Boolean {
        for (func in funcList) {
            if (this.symbolTable.get(func.ident.name) != null) {
                semanticErrorHandler.alreadyDefined(ctx, func.ident.name)
                return false
            }
            this.symbolTable.put(func.ident.name, func)
        }
        for (func in funcList) {
            func.check(this.symbolTable)
        }
        stat.check(this.symbolTable)
        return true
    }

    override fun accept(visitor: GenerateASTVisitor): List<Instruction> {
        return visitor.visitProgramAST(this)
    }
}