package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.type.JavaType

class TernaryNode(val boolExpression: BooleanExpressionNode,
                  val trueExpression: ExpressionNode,
                  val falseExpression: ExpressionNode
): ExpressionNode {
  override val type
    get() = if (trueExpression.type == falseExpression.type) trueExpression.type
  else TODO("Type 'merge' has not been implemented yet") // "merge" types (take the first common parent between the two classes if they are different)
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

}