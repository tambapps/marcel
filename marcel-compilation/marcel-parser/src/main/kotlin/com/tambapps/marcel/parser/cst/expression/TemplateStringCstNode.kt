package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class TemplateStringCstNode(
  val expressions: List<ExpressionCstNode>,
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken
) :
  AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

  override fun toString() = expressions.joinToString(separator = " + ")
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is TemplateStringCstNode) return false

    if (expressions != other.expressions) return false

    return true
  }

  override fun hashCode(): Int {
    return expressions.hashCode()
  }

}