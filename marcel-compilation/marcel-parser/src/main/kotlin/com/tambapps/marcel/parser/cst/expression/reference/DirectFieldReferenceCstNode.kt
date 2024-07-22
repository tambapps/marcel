package com.tambapps.marcel.parser.cst.expression.reference

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class DirectFieldReferenceCstNode(
  parent: CstNode?,
  override val value: String,
  token: LexToken
) : AbstractExpressionCstNode(parent, token) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

    override fun toString() = "@$value"

}