package com.tambapps.marcel.parser.cst.expression.reference

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.AbstractCstNode
import com.tambapps.marcel.parser.cst.CstNode

class IndexAccessCstNode(parent: CstNode?,
                         val ownerNode: CstNode, // the owner of the access, the 'a' in a[...]
                         val indexNodes: List<CstNode>,
                         val isSafeAccess: Boolean,
                         tokenStart: LexToken, tokenEnd: LexToken) :
    AbstractCstNode(parent, tokenStart, tokenEnd) {

    override fun toString() = StringBuilder().apply {
        append(ownerNode)
        if (isSafeAccess) append("?")
        indexNodes.joinTo(buffer = this, prefix = "[", postfix = "]")
    }.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as IndexAccessCstNode

        if (ownerNode != other.ownerNode) return false
        if (indexNodes != other.indexNodes) return false
        if (isSafeAccess != other.isSafeAccess) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + ownerNode.hashCode()
        result = 31 * result + indexNodes.hashCode()
        result = 31 * result + isSafeAccess.hashCode()
        return result
    }


}