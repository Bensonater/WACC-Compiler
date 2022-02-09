package frontend

import frontend.ast.ASTNode

class SymbolTable {
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

}