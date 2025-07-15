package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.visitor.ExpressionNodeVisitor

/**
 * Logical AND
 */
class AndNode(
  leftOperand: ExpressionNode,
  rightOperand: ExpressionNode
) :
  BooleanOperatorNode(leftOperand, rightOperand) {
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)
}