package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

/**
 * Do while statement node.
 */
class DoWhileStatementCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val statement: StatementCstNode,
  val condition: ExpressionCstNode
  ) :
  AbstractStatementCstNode(parent, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is DoWhileStatementCstNode) return false

    if (statement != other.statement) return false
    if (condition != other.condition) return false

    return true
  }

  override fun hashCode(): Int {
    var result = statement.hashCode()
    result = 31 * result + condition.hashCode()
    return result
  }
}