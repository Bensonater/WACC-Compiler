package frontend

import frontend.ast.ASTNode
import frontend.ast.type.TypeAST

open class SymbolTable {
    var symbolTable = HashMap<String, ASTNode>()
    var parent: SymbolTable? = null

    fun get(name: String): ASTNode? {
        return symbolTable[name]
    }

    fun put(name: String, astNode: ASTNode) {
        symbolTable[name] = astNode
    }

    fun setParentTable(st: SymbolTable) {
        parent = st
    }

    fun lookupAll(name: String): ASTNode? {
        var st = this
        while (true) {
            val ast = st.get(name)
            if (ast != null) {
                return ast
            }
            if (st.parent == null) {
                return null
            }
            st = st.parent!!
        }
    }

    fun identLookUp(name: String): ASTNode? {
        var st = this
        while (true) {
            val ast = st.get(name)
            if (ast != null) {
                return ast
            }
            if (st is FuncSymbolTable) {
                return null
            }
            if (st.parent == null) {
                return null
            }
            st = st.parent!!
        }
    }

    fun funcTypeLookUp(): TypeAST? {
        var st = this
        while (st !is FuncSymbolTable) {
            if (st.parent == null) {
                return null
            }
            st = st.parent!!
        }
        return st.type
    }


}

class FuncSymbolTable(val type: TypeAST) : SymbolTable()
