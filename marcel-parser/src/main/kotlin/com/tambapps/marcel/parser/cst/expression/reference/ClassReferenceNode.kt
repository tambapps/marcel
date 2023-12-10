package com.tambapps.marcel.parser.cst.expression.reference

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor

class ClassReferenceNode(
  parent: CstNode?,
  val type: TypeNode,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionNode(parent, tokenStart, tokenEnd) {
  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

    override fun toString() = "$value.class"
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    if (!super.equals(other)) return false

    other as ClassReferenceNode

    return type == other.type
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + type.hashCode()
    return result
  }

}