package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor

abstract class UnaryOperator(val operand: ExpressionNode): ExpressionNode {
  override val type: JavaType
    get() = operand.type
}

class UnaryMinus(operand: ExpressionNode) : UnaryOperator(operand) {

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }
}

class UnaryPlus(operand: ExpressionNode) : UnaryOperator(operand) {
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }
}