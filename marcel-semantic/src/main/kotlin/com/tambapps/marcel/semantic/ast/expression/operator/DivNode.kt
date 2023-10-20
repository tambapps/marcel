package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor

class DivNode(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryArithmeticOperatorNode(leftOperand, rightOperand) {

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) = visitor.visit(this)

}