package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode

class IsEqualNode(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  ComparisonOperatorNode(leftOperand, rightOperand) {
  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)
}