package com.tambapps.marcel.parser.cst.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class DoubleCstNode(parent: CstNode? = null, override val value: Double, token: LexToken) : AbstractExpressionCstNode(parent, token){

    override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

    override fun toString() = "${value}d"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DoubleCstNode) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

}