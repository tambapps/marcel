package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.lexer.LexToken

/**
 * Node for a block of statements
 */
class BlockStatementNode(val statements: MutableList<StatementNode>, tokenStart: LexToken, tokenEnd: LexToken) : AbstractStatementNode(tokenStart, tokenEnd) {

  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is BlockStatementNode) return false

    if (statements != other.statements) return false

    return true
  }

  override fun hashCode(): Int {
    return statements.hashCode()
  }

}