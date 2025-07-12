package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

/**
 * A wrapped expression. Useful for parenthesis expression, when checking where to put the NOT operator
 */
// TODO maybe useless
class WrappedExpressionCstNode(
  val expressionNode: ExpressionCstNode
): AbstractExpressionCstNode(expressionNode.parent, expressionNode.tokenStart, expressionNode.tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = expressionNode.accept(visitor, arg)



  override fun toString() = "($expressionNode)"
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is WrappedExpressionCstNode) return false

    if (expressionNode != other.expressionNode) return false

    return true
  }

  override fun hashCode(): Int {
    return expressionNode.hashCode()
  }
}