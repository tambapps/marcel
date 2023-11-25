package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode

class MethodParameterCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val name: String,
  val type: TypeCstNode,
  val defaultValue: ExpressionCstNode?,
  val annotations: List<AnnotationCstNode>,
  val thisParameter: Boolean
  ) :
  AbstractCstNode(parent, tokenStart, tokenEnd) {

  override fun toString(): String {
    if (thisParameter) return "this.$name"
    val builder = StringBuilder()
    builder.append(type)
      .append(" ")
      .append(name)
    if (defaultValue != null) {
      builder.append(" = ")
        .append(defaultValue)
    }
    return builder.toString()
  }
}