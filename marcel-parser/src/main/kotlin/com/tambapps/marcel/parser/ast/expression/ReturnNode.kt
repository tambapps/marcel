package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.ast.AstNodeVisitor

class ReturnNode(override val expression: ExpressionNode) : StatementNode {

  override fun accept(mv: AstNodeVisitor) {
    mv.visit(this)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ReturnNode) return false

    if (expression != other.expression) return false

    return true
  }

  override fun hashCode(): Int {
    return expression.hashCode()
  }
}