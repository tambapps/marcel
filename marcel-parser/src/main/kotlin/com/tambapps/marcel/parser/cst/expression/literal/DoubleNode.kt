package com.tambapps.marcel.parser.cst.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor

class DoubleNode(parent: CstNode? = null, override val value: Double, token: LexToken) : AbstractExpressionNode(parent, token){

    override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

    override fun toString() = "${value}d"

}