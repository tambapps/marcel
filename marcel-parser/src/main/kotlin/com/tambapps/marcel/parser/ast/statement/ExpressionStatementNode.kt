package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ExpressionVisitor

class ExpressionStatementNode(override val expression: ExpressionNode): StatementNode {

  override fun accept(mv: ExpressionVisitor) {
    mv.visit(this)
  }

  override fun toString(): String {
    return "$expression;"
  }
}