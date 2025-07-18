package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.symbol.type.JavaType

sealed class BinaryOperatorNode(
  val leftOperand: ExpressionNode,
  val rightOperand: ExpressionNode,
  override val type: JavaType
) : AbstractExpressionNode(
  leftOperand.tokenStart,
  rightOperand.tokenEnd
) {

  constructor(
    leftOperand: ExpressionNode,
    rightOperand: ExpressionNode
  )
      : this(leftOperand, rightOperand, JavaType.commonType(leftOperand, rightOperand))
}