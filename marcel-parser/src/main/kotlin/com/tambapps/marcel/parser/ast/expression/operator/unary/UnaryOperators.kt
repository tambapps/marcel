package com.tambapps.marcel.parser.ast.expression.operator.unary

import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ExpressionVisitor

abstract class UnaryOperator(val operand: ExpressionNode): ExpressionNode {
  override val type: JavaType
    get() = operand.type
}

class UnaryMinus(operand: ExpressionNode) : UnaryOperator(operand) {

  override fun accept(expressionVisitor: ExpressionVisitor) {
    expressionVisitor.visit(this)
  }
}

class UnaryPlus(operand: ExpressionNode) : UnaryOperator(operand) {
  override fun accept(expressionVisitor: ExpressionVisitor) {
    expressionVisitor.visit(this)
  }
}