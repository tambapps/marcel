package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor

abstract class UnaryOperator(val operand: ExpressionNode): ExpressionNode {

  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    operand.accept(visitor)
  }
}

class UnaryMinus(operand: ExpressionNode) : UnaryOperator(operand) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

}

class UnaryPlus(operand: ExpressionNode) : UnaryOperator(operand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

}

class NotNode(operand: ExpressionNode) : UnaryOperator(operand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

}

class IncrNode(val variableReference: ReferenceExpression, val amount: Int, val returnValueBefore: Boolean) : UnaryOperator(variableReference) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

}