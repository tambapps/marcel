package com.tambapps.marcel.parser.cst.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionNode
import com.tambapps.marcel.parser.cst.expression.ExpressionNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor

class BoolNode(parent: CstNode? = null, override val value: Boolean, token: LexToken) : AbstractExpressionNode(parent, token),
    ExpressionNode {
    override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

    override fun toString() = value.toString()

}