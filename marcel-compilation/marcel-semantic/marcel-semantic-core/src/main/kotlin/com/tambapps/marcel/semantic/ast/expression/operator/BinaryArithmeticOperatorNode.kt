package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.type.JavaPrimitiveType

sealed class BinaryArithmeticOperatorNode(leftOperand: com.tambapps.marcel.semantic.ast.expression.ExpressionNode, rightOperand: com.tambapps.marcel.semantic.ast.expression.ExpressionNode) :
  BinaryOperatorNode(leftOperand, rightOperand) {

  override val type: JavaPrimitiveType
    get() = super.type.asPrimitiveType // should always be a primitive
}