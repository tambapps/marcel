package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode

/**
 * Primitive Lower or Equal comparison
 */
class LeNode(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BooleanOperatorNode(leftOperand, rightOperand) {
  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)
}