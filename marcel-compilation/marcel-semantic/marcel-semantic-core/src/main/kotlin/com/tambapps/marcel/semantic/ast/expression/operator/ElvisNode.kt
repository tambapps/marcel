package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.type.JavaType

/**
 * Elvis node. The left operand SHOULD be a truthyCast of a DupNode of an Expression.
 * This is to optimize the compiler code
 */
class ElvisNode(
  leftOperand: ExpressionNode,
  rightOperand: ExpressionNode,
  type: JavaType
) :
  BinaryOperatorNode(leftOperand, rightOperand, type) {
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

}