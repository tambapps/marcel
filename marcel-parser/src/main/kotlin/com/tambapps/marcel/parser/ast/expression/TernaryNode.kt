package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.type.JavaType

open class TernaryNode(val boolExpression: BooleanExpressionNode,
                       val trueExpression: ExpressionNode,
                       val falseExpression: ExpressionNode
): ExpressionNode {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

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