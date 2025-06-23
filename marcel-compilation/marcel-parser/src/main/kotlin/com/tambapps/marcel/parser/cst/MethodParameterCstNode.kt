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
  val thisParameter: Boolean,
  val isNullable: Boolean,
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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    if (!super.equals(other)) return false

    other as MethodParameterCstNode

    if (name != other.name) return false
    if (type != other.type) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + name.hashCode()
    result = 31 * result + type.hashCode()
    return result
  }
}