package com.tambapps.marcel.parser.cst.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor

class FloatCstNode(parent: CstNode? = null, override val value: Float, token: LexToken) : AbstractExpressionCstNode(parent, token),
    ExpressionCstNode {
    override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

    override fun toString() = "${value}f"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FloatCstNode) return false
        if (!super.equals(other)) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

}