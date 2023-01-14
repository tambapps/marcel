package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.AstNodeVisitor

class ExpressionStatementNode(override val expression: ExpressionNode): StatementNode {

  override fun accept(mv: AstNodeVisitor) {
    mv.visit(this)
  }

  override fun toString(): String {
    return "$expression;"
  }
}