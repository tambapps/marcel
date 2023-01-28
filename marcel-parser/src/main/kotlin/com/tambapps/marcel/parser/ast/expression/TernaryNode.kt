package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.type.JavaType

class TernaryNode(val boolExpression: BooleanExpressionNode,
                  val trueExpression: ExpressionNode,
                  val falseExpression: ExpressionNode
): ExpressionNode {
  override val type
    get() = JavaType.commonType(trueExpression.type, falseExpression.type)
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    boolExpression.accept(visitor)
    trueExpression.accept(visitor)
    falseExpression.accept(visitor)
  }

  override fun toString(): String {
    return "$boolExpression ? $trueExpression : $falseExpression"
  }
}