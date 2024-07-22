package com.tambapps.marcel.parser.cst.expression.reference

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class ClassReferenceCstNode(
  parent: CstNode?,
  val type: TypeCstNode,
  tokenStart: LexToken,
  tokenEnd: LexToken
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {
  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

    override fun toString() = "$value.class"
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    if (!super.equals(other)) return false

    other as ClassReferenceCstNode

    return type == other.type
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + type.hashCode()
    return result
  }

}