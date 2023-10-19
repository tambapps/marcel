package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.type.JavaType

abstract class ComparisonOperatorNode(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(leftOperand, rightOperand, JavaType.boolean) {
}