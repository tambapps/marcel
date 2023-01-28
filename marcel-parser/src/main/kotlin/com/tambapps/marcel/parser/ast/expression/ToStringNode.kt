package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor
import com.tambapps.marcel.parser.type.JavaType

class ToStringNode(val expressionNode: ExpressionNode): ExpressionNode {
  override fun accept(astNodeVisitor: AstNodeVisitor) {
    astNodeVisitor.visit(this)
  }

  override val type = JavaType.String

  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    expressionNode.accept(visitor)
  }
}