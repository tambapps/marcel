package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.ExpressionVisitor

class ReturnNode(override val expression: ExpressionNode) : StatementNode {

  override fun accept(mv: ExpressionVisitor) {
    mv.visit(this)
  }
}