package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.lexer.LexToken

/**
 * Node for a block of statements
 */
class BlockStatementNode(val statements: MutableList<StatementNode>, tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractStatementNode(tokenStart, tokenEnd) {

  constructor(statements: MutableList<StatementNode>): this(statements, statements.first().tokenStart, statements.last().tokenEnd)

  override fun <T> accept(visitor: StatementNodeVisitor<T>) = visitor.visit(this)

  fun add(statementNode: StatementNode) = statements.add(statementNode)

  fun addAll(statements: Collection<StatementNode>) = this.statements.addAll(statements)
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