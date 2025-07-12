package com.tambapps.marcel.parser.cst.expression.reference

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class SuperReferenceCstNode(parent: CstNode?, token: LexToken): AbstractExpressionCstNode(parent, token) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

  override fun toString() = "super"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is SuperReferenceCstNode) return false
    return true
  }

  override fun hashCode(): Int {
    return javaClass.hashCode()
  }

}