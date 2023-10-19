package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode

/**
 * Primitive or Object reference comparison Not Equal
 */
class IsNotEqualNode(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  ComparisonOperatorNode(leftOperand, rightOperand) {
  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)
}