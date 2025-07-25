package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.visitor.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

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
  override val nullness = when {
    leftOperand.nullness == Nullness.NOT_NULL -> Nullness.NOT_NULL
    rightOperand.nullness == Nullness.NOT_NULL -> Nullness.NOT_NULL
    else -> Nullness.of(leftOperand, rightOperand)
  }
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

}