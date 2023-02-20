package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.ast.ComparisonOperator
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType

abstract class BinaryOperatorNode(val leftOperand: ExpressionNode, open val rightOperand: ExpressionNode): ExpressionNode {
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

class MulOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(leftOperand, rightOperand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand * $rightOperand"
  }
}

class DivOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(leftOperand, rightOperand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "$leftOperand / $rightOperand"
  }
}

class PlusOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(leftOperand, rightOperand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand + $rightOperand"
  }
}
class MinusOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(leftOperand, rightOperand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand - $rightOperand"
  }
}

class PowOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(leftOperand, rightOperand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "$leftOperand ^ $rightOperand"
  }
}

open class InvokeAccessOperator(leftOperand: ExpressionNode, final override val rightOperand: FunctionCallNode,
  val nullSafe: Boolean) :
    BinaryOperatorNode(leftOperand, rightOperand) {

  val scope: Scope get() = rightOperand.scope
  init {
    rightOperand.methodOwnerType = leftOperand
  }
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return if (nullSafe) "$leftOperand?.$rightOperand" else "$leftOperand.$rightOperand"
  }
}


open class GetFieldAccessOperator(leftOperand: ExpressionNode, override val rightOperand: ReferenceExpression,
                                  val nullSafe: Boolean) :
    BinaryOperatorNode(leftOperand, rightOperand) {
  val scope: Scope get() = rightOperand.scope

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "$leftOperand.$rightOperand"
  }
}

class ComparisonOperatorNode(val operator: ComparisonOperator, leftOperand: ExpressionNode, rightOperand: ExpressionNode) :
  BinaryOperatorNode(leftOperand, rightOperand) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand $operator $rightOperand"
  }
}

class AndOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode):
  BinaryOperatorNode(BooleanExpressionNode(leftOperand), BooleanExpressionNode(rightOperand)) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "$leftOperand && $rightOperand"
  }
}

class OrOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode):
  BinaryOperatorNode(BooleanExpressionNode(leftOperand), BooleanExpressionNode(rightOperand)) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand || $rightOperand"
  }
}

class LeftShiftOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode):
  BinaryOperatorNode(leftOperand, rightOperand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand << $rightOperand"
  }
}

class RightShiftOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode):
  BinaryOperatorNode(leftOperand, rightOperand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand << $rightOperand"
  }
}

class ElvisOperator(override var scope: Scope, leftOperand: ExpressionNode, rightOperand: ExpressionNode):
  BinaryOperatorNode(leftOperand, rightOperand), ScopedNode<Scope> {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand ?: $rightOperand"
  }
}

class IsOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode):
  BinaryOperatorNode(leftOperand, rightOperand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)


  override fun toString(): String {
    return "$leftOperand === $rightOperand"
  }
}

class IsNotOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode):
  BinaryOperatorNode(leftOperand, rightOperand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "$leftOperand !== $rightOperand"
  }
}

class FindOperator(leftOperand: ExpressionNode, rightOperand: ExpressionNode):
  BinaryOperatorNode(leftOperand, rightOperand) {
  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "$leftOperand =~ $rightOperand"
  }
}