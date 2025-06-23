package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
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
  override val nullness: Nullness
    get() = Nullness.of(leftOperand, rightOperand)
  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)

}