package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.type.JavaType

class BooleanExpressionNode(val innerExpression: ExpressionNode): ExpressionNode {

  override val type = JavaType.boolean

  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    innerExpression.accept(visitor)
  }
  override fun toString(): String {
    return if (innerExpression.type == JavaType.boolean) innerExpression.toString()
    else "marcelTruth($innerExpression)"
  }
}