package com.tambapps.marcel.parser.cst.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.StatementCstNodeVisitor

class WhileCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val condition: ExpressionCstNode,
  val statement: StatementCstNode
) :
  AbstractStatementCstNode(parent, tokenStart, tokenEnd) {
  override fun <T> accept(visitor: StatementCstNodeVisitor<T>) = visitor.visit(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is WhileCstNode) return false

    if (condition != other.condition) return false
    if (statement != other.statement) return false

    return true
  }

  override fun hashCode(): Int {
    var result = condition.hashCode()
    result = 31 * result + statement.hashCode()
    return result
  }
}