package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.scope.MethodScope

class ReturnNode(override var scope: MethodScope, override val expression: ExpressionNode) : StatementNode, ScopedNode<MethodScope> {

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

  override fun toString(): String {
    return "return $expression"
  }
}