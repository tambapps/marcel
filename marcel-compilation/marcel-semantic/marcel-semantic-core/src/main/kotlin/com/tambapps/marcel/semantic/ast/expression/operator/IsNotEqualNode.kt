package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor

/**
 * Primitive or Object reference comparison Not Equal
 */
class IsNotEqualNode(leftOperand: com.tambapps.marcel.semantic.ast.expression.ExpressionNode, rightOperand: com.tambapps.marcel.semantic.ast.expression.ExpressionNode) :
  BooleanOperatorNode(leftOperand, rightOperand) {
  override fun <T> accept(visitor: com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor<T>) = visitor.visit(this)

}