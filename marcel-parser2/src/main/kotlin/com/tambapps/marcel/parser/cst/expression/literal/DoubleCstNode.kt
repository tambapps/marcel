package com.tambapps.marcel.parser.cst.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor

class DoubleCstNode(parent: CstNode? = null, override val value: Double, token: LexToken) : AbstractExpressionCstNode(parent, token){

    override fun <T> accept(visitor: ExpressionCstNodeVisitor<T>) = visitor.visit(this)

    override fun toString() = "${value}d"

}