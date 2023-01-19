package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ComparisonOperator
import com.tambapps.marcel.parser.type.JavaType

abstract class BinaryOperatorNode(val leftOperand: ExpressionNode, val rightOperand: ExpressionNode): ExpressionNode {
  // for now only ints are handled
  override val type: JavaType = JavaType.int
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as BinaryOperatorNode

    if (leftOperand != other.leftOperand) return false
    if (rightOperand != other.rightOperand) return false
    if (type != other.type) return false

    return true
  }

  override fun hashCode(): Int {
    var result = leftOperand.hashCode()
    result = 31 * result + rightOperand.hashCode()
    result = 31 * result + type.hashCode()
    return result
  }
}

class MulOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(leftOperand, rightOperand) {
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "$leftOperand * $rightOperand"
  }
}

class DivOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(leftOperand, rightOperand) {
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "$leftOperand / $rightOperand"
  }
}

class PlusOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(leftOperand, rightOperand) {
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "$leftOperand + $rightOperand"
  }
}
class MinusOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(leftOperand, rightOperand) {
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "$leftOperand - $rightOperand"
  }
}

class PowOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(leftOperand, rightOperand) {
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "$leftOperand ^ $rightOperand"
  }
}

class AccessOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
    BinaryOperatorNode(leftOperand, rightOperand) {

  init {
    if (rightOperand is FunctionCallNode) {
      rightOperand.methodOwnerType = leftOperand
    }
  }

  override val type: JavaType
    get() = rightOperand.type
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "$leftOperand.$rightOperand"
  }
}

class ComparisonOperatorNode(tokenType: TokenType, leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(leftOperand, rightOperand) {

  val operator = ComparisonOperator.fromTokenType(tokenType)
  override val type = JavaType.boolean
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun toString(): String {
    return "$leftOperand <= $rightOperand"
  }
}
// TODO do all booleans operator