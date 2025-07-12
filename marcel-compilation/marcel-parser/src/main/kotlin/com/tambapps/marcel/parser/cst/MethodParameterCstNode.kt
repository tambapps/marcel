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

  override fun isEqualTo(node: CstNode): Boolean {

    if (node !is MethodParameterCstNode) return false

    if (thisParameter != node.thisParameter) return false
    if (isNullable != node.isNullable) return false
    if (name != node.name) return false
    if (!type.isEqualTo(node.type)) return false
    if (!IdentifiableCstNode.isEqualTo(defaultValue, node.defaultValue)) return false
    if (!IdentifiableCstNode.isEqualTo(annotations, node.annotations)) return false
    return true
  }
}