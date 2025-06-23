package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.symbol.type.JavaPrimitiveType
import com.tambapps.marcel.semantic.symbol.type.Nullness

sealed class BinaryArithmeticOperatorNode(
  leftOperand: ExpressionNode,
  rightOperand: ExpressionNode
) :
  BinaryOperatorNode(leftOperand, rightOperand) {

  override val nullness: Nullness
    get() = Nullness.NOT_NULL

  override val type: JavaPrimitiveType
    get() = super.type.asPrimitiveType // should always be a primitive
}