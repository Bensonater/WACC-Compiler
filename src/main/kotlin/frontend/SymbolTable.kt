package frontend

import frontend.ast.ASTNode
import frontend.ast.type.TypeAST

open class SymbolTable {
    var symbolTable = HashMap<String, ASTNode>()
    lateinit var parent: SymbolTable

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
        while (st != null) {
            var ast = st.get(name)
            if (ast != null) {
                return ast
            }
            st = st.parent
        }
        return null
    }

    fun identLookUp(name: String): ASTNode? {
        var st = this
        while (st != null) {
            var ast = st.get(name)
            if (ast != null) {
                return ast
            }
            if (st is FuncSymbolTable) {
                break
            }
            st = st.parent
        }
        return null
    }

    fun funcTypeLookUp(): TypeAST? {
        var st = this
        while (this !is FuncSymbolTable) {
            st = st.parent
            if (st == null) {
                return null
            }
        }
        return (st as FuncSymbolTable).type

    }


}

class FuncSymbolTable(val type: TypeAST) : SymbolTable()
