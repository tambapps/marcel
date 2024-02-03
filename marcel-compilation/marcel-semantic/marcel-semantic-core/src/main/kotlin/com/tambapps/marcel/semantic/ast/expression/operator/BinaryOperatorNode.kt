package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.type.JavaType

sealed class BinaryOperatorNode(
  val leftOperand: com.tambapps.marcel.semantic.ast.expression.ExpressionNode,
  val rightOperand: com.tambapps.marcel.semantic.ast.expression.ExpressionNode,
  type: JavaType
): com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode(type, leftOperand.tokenStart, rightOperand.tokenEnd) {

  constructor(leftOperand: com.tambapps.marcel.semantic.ast.expression.ExpressionNode, rightOperand: com.tambapps.marcel.semantic.ast.expression.ExpressionNode)
      : this(leftOperand, rightOperand, JavaType.commonType(leftOperand, rightOperand))
}