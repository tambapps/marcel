package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AbstractAstNode
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.ast.ComparisonOperator
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType

abstract class BinaryOperatorNode(
  token: LexToken,
  val leftOperand: ExpressionNode,
  open val rightOperand: ExpressionNode,
  val operatorMethodName: String?
):  AbstractExpressionNode(token) {

  constructor(token: LexToken, leftOperand: ExpressionNode, rightOperand: ExpressionNode): this(token, leftOperand, rightOperand, null)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as BinaryOperatorNode

    if (leftOperand != other.leftOperand) return false
    if (rightOperand != other.rightOperand) return false
    return true
  }

  override fun hashCode(): Int {
    var result = leftOperand.hashCode()
    result = 31 * result + rightOperand.hashCode()
    return result
  }

}

class MulOperator(token: LexToken, leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(token, leftOperand, rightOperand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand * $rightOperand"
  }
}

class DivOperator(token: LexToken, leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(token, leftOperand, rightOperand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "$leftOperand / $rightOperand"
  }
}

class PlusOperator(token: LexToken, leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  // TODO do others
  BinaryOperatorNode(token, leftOperand, rightOperand, "plus") {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand + $rightOperand"
  }
}
class MinusOperator(token: LexToken, leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(token, leftOperand, rightOperand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand - $rightOperand"
  }
}

class PowOperator(token: LexToken, leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(token, leftOperand, rightOperand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "$leftOperand ^ $rightOperand"
  }
}

open class InvokeAccessOperator constructor(token: LexToken, leftOperand: ExpressionNode, final override val rightOperand: FunctionCallNode,
  val nullSafe: Boolean) :
    BinaryOperatorNode(token, leftOperand, rightOperand) {

  val scope: Scope get() = rightOperand.scope
  init {
    rightOperand.methodOwnerType = leftOperand
  }
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return if (nullSafe) "$leftOperand?.$rightOperand" else "$leftOperand.$rightOperand"
  }
}


open class GetFieldAccessOperator(token: LexToken, leftOperand: ExpressionNode, override val rightOperand: ReferenceExpression,
                                  val nullSafe: Boolean) :
    BinaryOperatorNode(token, leftOperand, rightOperand) {
  val scope: Scope get() = rightOperand.scope

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "$leftOperand.$rightOperand"
  }
}

class ComparisonOperatorNode(token: LexToken, val operator: ComparisonOperator, leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(token, leftOperand, rightOperand) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand $operator $rightOperand"
  }
}

class AndOperator(token: LexToken, leftOperand: ExpressionNode, rightOperand: ExpressionNode):
  BinaryOperatorNode(token, BooleanExpressionNode.of(token, leftOperand), BooleanExpressionNode.of(token, rightOperand)) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "$leftOperand && $rightOperand"
  }
}

class OrOperator(token: LexToken, leftOperand: ExpressionNode, rightOperand: ExpressionNode):
  BinaryOperatorNode(token, BooleanExpressionNode.of(token, leftOperand), BooleanExpressionNode.of(token, rightOperand)) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand || $rightOperand"
  }
}

class LeftShiftOperator(token: LexToken, leftOperand: ExpressionNode, rightOperand: ExpressionNode):
  BinaryOperatorNode(token, leftOperand, rightOperand, "leftShift") {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand << $rightOperand"
  }
}

class RightShiftOperator(token: LexToken, leftOperand: ExpressionNode, rightOperand: ExpressionNode):
  BinaryOperatorNode(token, leftOperand, rightOperand, "rightShift") {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand << $rightOperand"
  }
}

class ElvisOperator(token: LexToken, override var scope: Scope, leftOperand: ExpressionNode, rightOperand: ExpressionNode):
  BinaryOperatorNode(token, leftOperand, rightOperand), ScopedNode<Scope> {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand ?: $rightOperand"
  }
}

class IsOperator(token: LexToken, leftOperand: ExpressionNode, rightOperand: ExpressionNode):
  BinaryOperatorNode(token, leftOperand, rightOperand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand === $rightOperand"
  }
}

class IsNotOperator(token: LexToken, leftOperand: ExpressionNode, rightOperand: ExpressionNode):
  BinaryOperatorNode(token, leftOperand, rightOperand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "$leftOperand !== $rightOperand"
  }
}

class FindOperator(token: LexToken, leftOperand: ExpressionNode, rightOperand: ExpressionNode):
  BinaryOperatorNode(token, leftOperand, rightOperand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "$leftOperand =~ $rightOperand"
  }
}