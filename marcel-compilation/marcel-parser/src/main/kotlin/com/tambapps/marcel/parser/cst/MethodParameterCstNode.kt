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
  AbstractCstNode(parent, tokenStart, tokenEnd), IdentifiableCstNode {

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

  override fun isSyntaxEqualTo(other: CstNode): Boolean {

    if (other !is MethodParameterCstNode) return false

    if (thisParameter != other.thisParameter) return false
    if (isNullable != other.isNullable) return false
    if (name != other.name) return false
    if (type notEq other.type) return false
    if (defaultValue notEq other.defaultValue) return false
    if (annotations notEq other.annotations) return false
    return true
  }
}