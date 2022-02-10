package frontend

import frontend.ast.ASTNode
import frontend.ast.IdentAST

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

    fun identLookUp(name: String): ASTNode?{
        var st = this
        while (st != null) {
            var ast = st.get(name)
            if (ast != null) {
                return ast
            }
            if (st is FuncSymbolTable){
                break
            }
            st = st.parent
        }
        return null
    }


}

class FuncSymbolTable(val ident: IdentAST) : SymbolTable()
