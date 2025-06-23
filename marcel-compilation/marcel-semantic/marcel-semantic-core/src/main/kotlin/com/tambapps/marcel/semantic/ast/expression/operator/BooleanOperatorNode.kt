package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

abstract class BooleanOperatorNode(
  leftOperand: ExpressionNode,
  rightOperand: ExpressionNode
) :
  BinaryOperatorNode(leftOperand, rightOperand, JavaType.boolean) {
  override val nullness: Nullness
    get() = Nullness.NOT_NULL
  }