package com.tambapps.marcel.parser.cst.expression

/**
 * A wrapped expression. Useful for parenthesis expression, when checking where to put the NOT operator
 */
class WrappedExpressionCstNode(
  val expressionNode: ExpressionCstNode
): AbstractExpressionCstNode(expressionNode.parent, expressionNode.tokenStart, expressionNode.tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = expressionNode.accept(visitor, arg)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is WrappedExpressionCstNode) return false
    if (!super.equals(other)) return false

    if (expressionNode != other.expressionNode) return false

    return true
  }

  override fun hashCode(): Int {
    return expressionNode.hashCode()
  }

  override fun toString() = "($expressionNode)"
}