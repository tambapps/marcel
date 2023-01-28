package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstVisitor

class ExpressionStatementNode(override val expression: ExpressionNode): StatementNode {

  override fun accept(mv: AstNodeVisitor) {
    mv.visit(this)
  }

  override fun accept(visitor: AstVisitor) {
    super.accept(visitor)
    expression.accept(visitor)
  }
  override fun toString(): String {
    return "$expression;"
  }
}