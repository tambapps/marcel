package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode

class VariableAssignmentCstNode(override val value: String,
                                val expressionNode: CstExpressionNode,
                                parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T> accept(visitor: ExpressionCstNodeVisitor<T>) = visitor.visit(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    if (!super.equals(other)) return false

    other as VariableAssignmentCstNode

    if (value != other.value) return false
    if (expressionNode != other.expressionNode) return false

    return true
  }

  override fun hashCode(): Int {
    var result = super.hashCode()
    result = 31 * result + value.hashCode()
    result = 31 * result + expressionNode.hashCode()
    return result
  }

  override fun toString(): String {
    return "$value = $expressionNode"
  }
}