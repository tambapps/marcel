package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.type.JavaType

class DivNode(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryArithmeticOperatorNode(leftOperand, rightOperand) {
  override fun <T> accept(visitor: AstNodeVisitor<T>) = visitor.visit(this)
}