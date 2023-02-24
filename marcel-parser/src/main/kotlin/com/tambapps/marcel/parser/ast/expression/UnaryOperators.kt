package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor

abstract class UnaryOperator(token: LexToken, val operand: ExpressionNode): AbstractExpressionNode(token) {

}

class UnaryMinus(token: LexToken, operand: ExpressionNode) : UnaryOperator(token, operand) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

}

class UnaryPlus(token: LexToken, operand: ExpressionNode) : UnaryOperator(token, operand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

}

class NotNode(token: LexToken, operand: ExpressionNode) : UnaryOperator(token, operand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

}

class IncrNode(token: LexToken, val variableReference: ReferenceExpression, val amount: Int, val returnValueBefore: Boolean) : UnaryOperator(token, variableReference) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

}