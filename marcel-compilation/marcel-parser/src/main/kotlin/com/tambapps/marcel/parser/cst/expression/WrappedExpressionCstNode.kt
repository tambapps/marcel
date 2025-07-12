package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

/**
 * A wrapped expression. Useful for parenthesis expression, when checking where to put the NOT operator
 */
class WrappedExpressionCstNode constructor(
  val expressionNode: ExpressionCstNode
): AbstractExpressionCstNode(expressionNode.parent, expressionNode.tokenStart, expressionNode.tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

  override fun toString() = "($expressionNode)"

}